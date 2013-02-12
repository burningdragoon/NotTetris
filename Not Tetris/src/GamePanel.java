import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int PWIDTH = 600;
	private static final int PHEIGHT = 500;
	
	private static final int NO_DELAYS_PER_YIELD = 16;
	/* Number of frames with a delay of 0 ms before the animation thread yields
	     to other running threads. */
	private static final int MAX_FRAME_SKIPS = 5;
	// no. of frames that can be skipped in any one animation loop
	// i.e the games state is updated but not rendered
	
	
	private Thread animator;
	private volatile boolean running = false;
	private volatile boolean isPaused = false;
	
	private long period;
	
	
	private Game game;
	
	private long gameStartTime;
	//private int timeSpentInGame;
	
	private volatile boolean gameOver = false;
	
	private Font msgsFont;
	private FontMetrics metrics;
	
	// off-screen rendering
	private Graphics dbg;
	private BufferedImage dbImage = null;
	
	// Game specific variables...
	private final int BLOCK_SIZE = 15;
	private final int BOARD_WIDTH = 10;
	private final int BOARD_HEIGHT = 35;
	private final int GAME_OVER_ZONE = 5;
	// Game States
	private final int NO_BLOCK_FALLING = 0;
	private final int BLOCK_FALLING = 1;
	private final int CHECKING_ROWS = 2;
	private final int CLEARING_ROWS = 3;
	
	private int gameState;
	private boolean[][] gameBoard;
	private Color[][] gameBoardColor;
	private boolean[] clearedRows;
	
	// Coordinates of the game board
	private int boardCoordinateX;
	private int boardCoordinateY;
	
	private Block activeBlock;
	
	private int updateDelay = 60;
	private int updateDelayCount = 0;
	
	private Random random;
	
	private int score;
	private int levelUp;
	private int scoreLevelBarrier;
	
	public GamePanel(Game game, long period)
	{
		this.period = period;
		
		setDoubleBuffered(false);
		setBackground(Color.black);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		
		setFocusable(true);
		requestFocus();
		
		isPaused = true;
		
		//set up message showing stuff
		msgsFont = new Font("SansSerif", Font.BOLD, 24);
		metrics = this.getFontMetrics(msgsFont);
		
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{
				processKey(e);
			}
			
		});
		
		//game specifc stuff...
		boardCoordinateX = (PWIDTH / 2) - ((BOARD_WIDTH / 2) * BLOCK_SIZE);
		boardCoordinateY = 0 - (GAME_OVER_ZONE * BLOCK_SIZE);
		
		NewGame();
		
		/*
		gameBoard = new boolean[BOARD_WIDTH][BOARD_HEIGHT];
		gameBoardColor = new Color[BOARD_WIDTH][BOARD_HEIGHT];
		clearedRows = new boolean[BOARD_HEIGHT];
		
		activeBlock = null;
		
		random = new Random();
		
		AddBlock();
		*/
		
	}
	
	private void processKey(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		
		if(gameState == BLOCK_FALLING)
		{
			switch(keyCode)
			{
			case KeyEvent.VK_UP:
				tryRotateBlock();
				break;
			case KeyEvent.VK_LEFT:
				if(canMoveBlockLeft())
					activeBlock.moveBlockLeft();
				break;
			case KeyEvent.VK_RIGHT:
				if(canMoveBlockRight())
					activeBlock.moveBlockRight();
				break;
			case KeyEvent.VK_DOWN:
				if(canMoveBlockDown())
					activeBlock.moveBlockDown();
				break;
			}
		}
		else if(gameOver)
		{
			if(keyCode == KeyEvent.VK_ENTER)
			{
				gameOver = false;
				NewGame();
			}
		}
			
	}
	
	public void addNotify()
	  // wait for the JPanel to be added to the JFrame before starting
	  { super.addNotify();   // creates the peer
	    startGame();         // start the thread
	  }	
	
	private void startGame()
	  // initialise and start the thread 
	  { 
	    if (animator == null || !running) {
	      animator = new Thread(this);
		  animator.start();
	    }
	  } // end of startGame()
	
	public void gameOver()
	  { 
	    if (!gameOver)
	      gameOver = true; 
	  } // end of gameOver()
	
	// ------------- game life cycle methods ------------
	// called by the JFrame's window listener methods


	public void resumeGame()
	// called when the JFrame is activated / deiconified
	{ isPaused = false;  } 
	

	public void pauseGame()
	// called when the JFrame is deactivated / iconified
	{ isPaused = true;   } 


	public void stopGame() 
	// called when the JFrame is closing
	{  running = false;  }

	// ----------------------------------------------
	
	public void run()
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0l;
		int noDelays = 0;
		long excess = 0;
		
		gameStartTime = System.nanoTime();
		beforeTime = gameStartTime;
		
		running = true;
		
		
		while(running)
		{
			gameUpdate();
			gameRender();
			paintScreen();
			
			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;  

			if (sleepTime > 0) {   // some time left in this cycle
				try {
					Thread.sleep(sleepTime/1000000L);  // nano -> ms
		        }
		        catch(InterruptedException ex){}
		        	overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
		      	}
			else {    // sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime;  // store excess time value
				overSleepTime = 0L;

		        if (++noDelays >= NO_DELAYS_PER_YIELD) {
		        	Thread.yield();   // give another thread a chance to run
		        	noDelays = 0;
		        }
			}

			//beforeTime = J3DTimer.getValue();
			beforeTime = System.nanoTime();

			/* If frame animation is taking too long, update the game state
		         without rendering it, to get the updates/sec nearer to
		         the required FPS. */
			int skips = 0;
			while((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
			    gameUpdate();    // update state but don't render
		        skips++;
			}
		}
		System.exit(0);   // so window disappears
	}
	
	private void gameUpdate()
	{
		if(!gameOver)
		{
			switch(gameState)
			{
				case CHECKING_ROWS:
					checkRowsForClear();
					break;
				case CLEARING_ROWS:
					clearRows();
					break;
				case BLOCK_FALLING:
					if(updateDelayCount++ >= updateDelay)
					{
						updateDelayCount = 0;
						//System.out.println(displayBoardAsString());
						if(canMoveBlockDown())
						{
							activeBlock.moveBlockDown();
						}
						else
						{
							freezeActiveBlock();
							checkGameOver();
							//checkRowsForClear();
						}
					}
					break;
				case NO_BLOCK_FALLING:
					AddBlock();
					break;
			}
		}
	}
	
	private void gameRender()
	{
		if (dbImage == null){
			  dbImage = (BufferedImage) createImage(PWIDTH, PHEIGHT);
			  if (dbImage == null) {
				  System.out.println("dbImage is null");
				  return;
			  }
			  else
				  dbg = dbImage.getGraphics();
		}
		dbg.setColor(Color.white);
		dbg.fillRect(0,0,PWIDTH, PHEIGHT);
		//dbg.drawImage(bgImage, bgPosition, 0, this);
		
		dbg.setColor(Color.black);
		dbg.drawString("Definitely Not Tetris", 50, 50);
		
		dbg.drawString("Score: " + score, PWIDTH - 100, 50);
		
		drawBoard(dbg);

		if(gameOver)
		{
			dbg.setColor(Color.black);
			dbg.drawString("GAME OVER", 50, 100);
			dbg.drawString("Hit Enter to try again.", 50, 120);
		}
	}
	
	private void paintScreen()
	{
	    Graphics g;
	    try {
	      g = this.getGraphics();
	      if ((g != null) && (dbImage != null))
	        g.drawImage(dbImage, 0, 0, null);
	      // Sync the display on some systems.
	      // (on Linux, this fixes event queue problems)
	      Toolkit.getDefaultToolkit().sync();
	      g.dispose();
	    }
	    catch (Exception e)
	    { System.out.println("Graphics context error: " + e);  }		
	}
	
	private void NewGame()
	{	
		gameBoard = new boolean[BOARD_WIDTH][BOARD_HEIGHT];
		gameBoardColor = new Color[BOARD_WIDTH][BOARD_HEIGHT];
		clearedRows = new boolean[BOARD_HEIGHT];
		
		activeBlock = null;
		
		random = new Random();
		
		score = 0;
		levelUp = 0;
		scoreLevelBarrier = 1000;
		
		updateDelay = 60;
		updateDelayCount = 0;
		
		
		AddBlock();
	}
	
	private int[][] getActiveBlockCoordinates()
	{
		int[][] blockPosition = activeBlock.getBlockPosition();
		
		int xCoord1 = blockPosition[0][0];
		int yCoord1 = blockPosition[0][1];
		
		int xCoord2 = blockPosition[1][0] + xCoord1;
		int yCoord2 = blockPosition[1][1] + yCoord1;
		
		int xCoord3 = blockPosition[2][0] + xCoord1;
		int yCoord3 = blockPosition[2][1] + yCoord1;
		
		int xCoord4 = blockPosition[3][0] + xCoord1;
		int yCoord4 = blockPosition[3][1] + yCoord1;
		
		int[][] coordinates = new int[blockPosition.length][blockPosition[0].length];
		
		coordinates[0][0] = xCoord1;
		coordinates[0][1] = yCoord1;
		
		coordinates[1][0] = xCoord2;
		coordinates[1][1] = yCoord2;
		
		coordinates[2][0] = xCoord3;
		coordinates[2][1] = yCoord3;
		
		coordinates[3][0] = xCoord4;
		coordinates[3][1] = yCoord4;
		
		
		return coordinates;
	}
	
	private void AddBlock()
	{
		activeBlock = new Block(random.nextInt(7), 4, 0);
		//activeBlock = new Block(0, 4, 1);
		gameState = BLOCK_FALLING;
	}
	
	
	private boolean canMoveBlockDown()
	{
		int[][] coordinates = getActiveBlockCoordinates();
		
		//if any space below the active block is NOT empty or the block is at the bottom of the board, the block cannot move down and will get stuck
		
		// At the bottom
		if(coordinates[0][1] + 1 == BOARD_HEIGHT
				|| coordinates[1][1] + 1 == BOARD_HEIGHT
				|| coordinates[2][1] + 1 == BOARD_HEIGHT
				|| coordinates[3][1] + 1 == BOARD_HEIGHT)
		{
			//System.out.println("At bottom!");
			return false;
		}
		// Below Space is filled
		else if(gameBoard[coordinates[0][0]][coordinates[0][1] + 1]
				|| gameBoard[coordinates[1][0]][coordinates[1][1] + 1]
				|| gameBoard[coordinates[2][0]][coordinates[2][1] + 1]
				|| gameBoard[coordinates[3][0]][coordinates[3][1] + 1])
		{
			//System.out.println("Can't Move Down!");
			return false;
		}
		else
			return true;
	}
	
	private boolean canMoveBlockLeft()
	{
		int[][] coordinates = getActiveBlockCoordinates();
		
		//at left edge
		if(coordinates[0][0] <= 0
				|| coordinates[1][0] <= 0
				|| coordinates[2][0] <= 0
				|| coordinates[3][0] <= 0)
		{
			return false;
		}
		//left space is filled
		else if(gameBoard[coordinates[0][0] - 1][coordinates[0][1]]
				|| gameBoard[coordinates[1][0] - 1][coordinates[1][1]]
				|| gameBoard[coordinates[2][0] - 1][coordinates[2][1]]
				|| gameBoard[coordinates[3][0] - 1][coordinates[3][1]])
		{
			return false;
		}
		else
			return true;
	}
	
	private boolean canMoveBlockRight()
	{
		int[][] coordinates = getActiveBlockCoordinates();
		
		//at left edge
		if(coordinates[0][0] >= BOARD_WIDTH -1
				|| coordinates[1][0] >= BOARD_WIDTH -1
				|| coordinates[2][0] >= BOARD_WIDTH -1
				|| coordinates[3][0] >= BOARD_WIDTH -1)
		{
			return false;
		}
		//left space is filled
		else if(gameBoard[coordinates[0][0] + 1][coordinates[0][1]]
				|| gameBoard[coordinates[1][0] + 1][coordinates[1][1]]
				|| gameBoard[coordinates[2][0] + 1][coordinates[2][1]]
				|| gameBoard[coordinates[3][0] + 1][coordinates[3][1]])
		{
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Attempts to rotate active block. If new position has conflicts, try next position.
	 * Eventually, the block will rotate to a acceptable posistion or revert to original position
	 */
	private void tryRotateBlock()
	{
		activeBlock.RotateBlock();
		boolean conflict = false;
		
		int[][] coordinates = getActiveBlockCoordinates();
		
		// block conflicts with left border
		if(coordinates[0][0] < 0
				|| coordinates[1][0] < 0
				|| coordinates[2][0] < 0
				|| coordinates[3][0] < 0)
		{
			conflict = true;
		}
		// block conflicts with right border
		else if(coordinates[0][0] >= BOARD_WIDTH
				|| coordinates[1][0] >= BOARD_WIDTH
				|| coordinates[2][0] >= BOARD_WIDTH
				|| coordinates[3][0] >= BOARD_WIDTH)
		{
			conflict = true;
		}	
		// block conflicts with bottom border
		else if(coordinates[0][1] >= BOARD_HEIGHT
				|| coordinates[1][1] >= BOARD_HEIGHT
				|| coordinates[2][1] >= BOARD_HEIGHT
				|| coordinates[3][1] >= BOARD_HEIGHT)
		{
			conflict = true;
		}
		// block conflicts with filled space
		else if(gameBoard[coordinates[0][0]][coordinates[0][1]]
				|| gameBoard[coordinates[1][0]][coordinates[1][1]]
				|| gameBoard[coordinates[2][0]][coordinates[2][1]]
				|| gameBoard[coordinates[3][0]][coordinates[3][1]]
				)
		{
			conflict = true;
		}
		
		
		if(conflict)
			tryRotateBlock();
	}
	
	/**
	 * Freeze active block in place and remove it
	 */
	private void freezeActiveBlock()
	{
		int[][] coordinates = getActiveBlockCoordinates();
		
		for(int x = 0; x < coordinates.length; x++)
		{
			gameBoard[coordinates[x][0]][coordinates[x][1]] = true;
			gameBoardColor[coordinates[x][0]][coordinates[x][1]] = activeBlock.getBlockColor();
		}
		gameState = CHECKING_ROWS;
	}
	
	/**
	 * Check if any rows can be cleared
	 */
	private void checkRowsForClear()
	{
		boolean cleared = false;
		
		for(int row = 0; row < BOARD_HEIGHT; row++)
		{
			boolean fullRow = true;
			for(int col = 0; col < BOARD_WIDTH; col++)
			{
				if(!gameBoard[col][row])
				{
					fullRow = false;
					break;
				}
			}
			
			if(fullRow)
			{
				cleared = true;
				clearedRows[row] = true;
			}
		}
		
		
		if(cleared)
			gameState = CLEARING_ROWS;
		else
			gameState = NO_BLOCK_FALLING;
	}
	
	/**
	 * Clears the full rows and moves remaining rows down
	 */
	private void clearRows()
	{
		for(int index = 0; index < clearedRows.length; index++)
		{
			if(clearedRows[index])
			{
				for(int col = 0; col < BOARD_WIDTH; col++)
				{
					//gameBoardColor[col][index] = Color.white;
					gameBoard[col][index] = false;
				}
				//clearedRows[index] = false;
			}
		}
		
		dropRows();
		
		gameState = CHECKING_ROWS;
	}
	
	/**
	 * After clearing all rows, drop all floating pieces to lowest possible position.
	 * Also, reset the rows marked as clear.
	 */
	private void dropRows()
	{
		int clearRowCount = 0;
		
		for(int index = BOARD_HEIGHT - 1; index >= 0; index--)
		{
			if(clearedRows[index])
			{
				clearRowCount++;
				clearedRows[index] = false;
			}
			else if(clearRowCount > 0)
			{
				//add score...
				//score += (clearRowCount * BOARD_WIDTH) * clearRowCount;
				updateScore(clearRowCount);
				
				dropRows(index, clearRowCount);
				clearRowCount = 0;
				
			}
		}
	}
	
	/**
	 * drops all rows in all arrays 
	 * @param startRow
	 */
	private void dropRows(int startRow, int rowCount)
	{
		//System.out.println("Drop Rows: " + startRow + " - " + rowCount);
		for(int row = startRow; row >= 0; row--)
		{
			for(int col = 0; col < BOARD_WIDTH; col++)
			{
				boolean tempBoolean = gameBoard[col][row];
				gameBoard[col][row + rowCount] = tempBoolean;
				gameBoard[col][row] = false;
				
				Color tempColor = gameBoardColor[col][row];
				gameBoardColor[col][row + rowCount] = tempColor;
				gameBoardColor[col][row] = Color.black;
			}		
			boolean tempBoolean = clearedRows[row];
			clearedRows[row + rowCount] = tempBoolean;
			clearedRows[row] = false;
		}
	}
	
	/**
	 * Updates the score and increases drop speed after a certain score has been obtained
	 * @param rows
	 */
	private void updateScore(int rows)
	{
		int points = (rows * BOARD_WIDTH * 10) * (rows * rows); 
		score += points;
		
		levelUp += points;
		if(levelUp > scoreLevelBarrier)
		{
			updateDelay -= 5;
		}
	}
	
	/**
	 * Check if the current state of the board is in the lose condition
	 * Game Over occurs when any block has been frozen while in the Game Over Zone
	 */
	private void checkGameOver()
	{
		for(int x = 0; x < BOARD_WIDTH; x++)
		{
			for(int y = 0; y < GAME_OVER_ZONE; y++)
			{
				if(gameBoard[x][y])
				{
					gameOver();
					return;
				}
			}
		}
	}
	
	private void drawBoard(Graphics g)
	{
		int[][] blockCoordinates = getActiveBlockCoordinates();
		
		for(int x = 0; x < BOARD_WIDTH; x++)
		{
			for(int y = 0; y < BOARD_HEIGHT; y++)
			{
				if(gameBoard[x][y])
				{
					//fill with board color
					//System.out.println("Board Piece!");
					drawBoardPiece(g, x, y, gameBoardColor[x][y]);
				}
				else if((blockCoordinates[0][0] == x && blockCoordinates[0][1] == y)
						|| (blockCoordinates[1][0] == x && blockCoordinates[1][1] == y)
						|| (blockCoordinates[2][0] == x && blockCoordinates[2][1] == y)
						|| (blockCoordinates[3][0] == x && blockCoordinates[3][1] == y))
				{
					//fill with block color
					//System.out.println("Block Piece!");
					drawBoardPiece(g, x, y, activeBlock.getBlockColor());
				}
				else
				{
					//System.out.println("Empty Board!");
					drawBoardPiece(g, x, y, Color.black);
				}
			}
		}
		
		//Draw the board border
		g.setColor(Color.black);
		g.drawRect(boardCoordinateX, boardCoordinateY, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
		
	}
	
	private void drawBoardPiece(Graphics g, int xCoord, int yCoord, Color color)
	{
		int x = boardCoordinateX + (xCoord * BLOCK_SIZE);
		int y = boardCoordinateY + (yCoord * BLOCK_SIZE);
		int width = BLOCK_SIZE;
		int height = BLOCK_SIZE;
		
		g.setColor(color);
		g.fillRect(x, y, width, height);
		
		g.setColor(Color.black);
		g.drawRect(x, y, width, height);
		
		//System.out.println("Painting: " + xCoord + "," + yCoord);
	}

	private String displayBoardAsString()
	{
		String result = "";
		
		int[][] blockCoordinates = getActiveBlockCoordinates();
		
		for(int x = 0; x < BOARD_WIDTH; x++)
		{
			for(int y = 0; y < BOARD_HEIGHT; y++)
			{
				if(gameBoard[x][y])
				{
					result += "[_]";
				}
				else if((blockCoordinates[0][0] == x && blockCoordinates[0][1] == y)
						|| (blockCoordinates[1][0] == x && blockCoordinates[1][1] == y)
						|| (blockCoordinates[2][0] == x && blockCoordinates[2][1] == y)
						|| (blockCoordinates[3][0] == x && blockCoordinates[3][1] == y))
				{
					result += "[B]";
				}
				else
				{
					result += "[ ]";
				}
			}
			result += "\n";
		}
		
		return result;
	}
	
}
