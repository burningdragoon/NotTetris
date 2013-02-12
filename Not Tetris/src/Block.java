import java.awt.Color;

public class Block {

	// Block types
	private static final int LINE_BLOCK = 0;
	private static final int SQUARE_BLOCK = 1;
	private static final int Z_BLOCK = 2;
	private static final int S_BLOCK = 3;
	private static final int T_BLOCK = 4;
	private static final int L_BLOCK = 5;
	private static final int REVERSE_L_BLOCK = 6;
	
	/*
	 * Offset 0: position of block's main piece in relation to Tetris board
	 * Offset 1-3: relative position to main piece (offset 0)
	 */
	private int[][] blockPosition;
	private int blockType;
	private Color blockColor;
	private int currentRotation;
	
	public Block(int type, int x, int y)
	{
		blockType = type;
		blockPosition = new int[4][2];
		currentRotation = 0;
		
		//blockColor = Color.blue;
		
		blockPosition[0][0] = x;
		blockPosition[0][1] = y;
		
		switch(blockType)
		{
			case LINE_BLOCK:
				setupBlock(0,-1, 0,1, 0,2);
				blockColor = Color.blue;
				break;
			case SQUARE_BLOCK:
				setupBlock(1,0, 0,1, 1,1);
				blockColor = Color.red;
				break;
			case Z_BLOCK:
				setupBlock(-1,0, 0,1, 1,1);
				blockColor = Color.cyan;
				break;
			case S_BLOCK:
				setupBlock(1,0, 0,1, -1,1);
				blockColor = Color.yellow;
				break;
			case T_BLOCK:
				setupBlock(-1,0, 0,1, 1,0);
				blockColor = Color.green;
				break;
			case L_BLOCK:
				setupBlock(0,-1, 0,1, 1,1);
				blockColor = Color.orange;
				break;
			case REVERSE_L_BLOCK:
				setupBlock(0,-1, 0,1, -1,1);
				blockColor = Color.magenta;
				break;
			default:
				break;
		}
	}
	
	public void setupBlock(int xOffset1, int yOffset1, int xOffset2, int yOffset2, int xOffset3, int yOffset3)
	{
		blockPosition[1][0] = xOffset1;
		blockPosition[1][1] = yOffset1;
		
		blockPosition[2][0] = xOffset2;
		blockPosition[2][1] = yOffset2;
		
		blockPosition[3][0] = xOffset3;
		blockPosition[3][1] = yOffset3;
	}
	
	
	public void RotateBlock()
	{
		int x1 = 0;
		int y1 = 0;
		
		int x2 = 0;
		int y2 = 0;
		
		int x3 = 0;
		int y3 = 0;
		
		switch(blockType)
		{
			case LINE_BLOCK:
				switch(currentRotation)
				{
					case 0:
						x1 = -1;
						y1 = 0;
						
						x2 = 1;
						y1 = 0;
						
						x3 = 2;
						y3 = 0;
						setupBlock(x1,y1, x2,y2, x3,y3);
						currentRotation++;
						break;
					case 1:
						x1 = 0;
						y1 = -1;
						
						x2 = 0;
						y2 = 1;
						
						x3 = 0;
						y3 = 2;
						setupBlock(x1,y1, x2,y2, x3,y3);
						currentRotation = 0;
						break;
					default:
						break;
						
				}
				break;
			
			case SQUARE_BLOCK:
				break;
				
			case Z_BLOCK:
				switch(currentRotation)
				{
					case 0:
						x1 = 0;
						y1 = -1;
						
						x2 = -1;
						y2 = 0;
						
						x3 = -1;
						y3 = 1;
						setupBlock(x1,y1, x2,y2, x3,y3);
						currentRotation++;
						break;
					case 1:
						x1 = -1;
						y1 = 0;
						
						x2 = 0;
						y2 = 1;
						
						x3 = 1;
						y3 = 1;
						setupBlock(-1,0, 0,1, 1,1);
						currentRotation = 0;
						break;
				}
				break;
				
			case S_BLOCK:
				switch(currentRotation)
				{
					case 0:
						setupBlock(0,-1, 1,0, 1,1);
						currentRotation++;
						break;
					case 1:
						setupBlock(1,0, 0,1, -1,1);
						currentRotation = 0;
						break;
				}
				break;
				
			case T_BLOCK:
				switch(currentRotation)
				{
					case 0:
						setupBlock(0,-1, -1,0, 0,1);
						currentRotation++;
						break;
					case 1:
						setupBlock(-1,0, 0,-1, 1,0);
						currentRotation++;
						break;
					case 2:
						setupBlock(0,-1, 1,0, 0,1);
						currentRotation++;
						break;
					case 3:
						setupBlock(-1,0, 0,1, 1,0);
						currentRotation = 0;
						break;
				}
				break;
				
			case L_BLOCK:
				switch(currentRotation)
				{
					case 0:
						setupBlock(-1,0, -1,1, 1,0);
						currentRotation++;
						break;
					case 1:
						setupBlock(-1,-1, 0,-1, 0,1);
						currentRotation++;
						break;
					case 2:
						setupBlock(-1,0, 1,0, 1,-1);
						currentRotation++;
						break;
					case 3:
						setupBlock(0,-1, 0,1, 1,1);
						currentRotation = 0;
						break;
				}
				break;
				
			case REVERSE_L_BLOCK:
				switch(currentRotation)
				{
					case 0:
						setupBlock(-1,0, -1,-1, 1,0);
						currentRotation++;
						break;
					case 1:
						setupBlock(0,-1, 1,-1, 0,1);
						currentRotation++;
						break;
					case 2:
						setupBlock(-1,0, 1,0, 1,1);
						currentRotation++;
						break;
					case 3:
						setupBlock(0,-1, 0,1, -1,1);
						currentRotation = 0;
						break;
				}
				break;
				
			default:
				break;
		}
	}
	
	public void moveBlockDown()
	{
		blockPosition[0][1] = blockPosition[0][1] + 1;
	}
	
	public void moveBlockRight()
	{
		blockPosition[0][0] = blockPosition[0][0] + 1;
	}
	
	public void moveBlockLeft()
	{
		blockPosition[0][0] = blockPosition[0][0] - 1;
	}
	
	public int[][] getBlockPosition()
	{
		return blockPosition;
	}
	
	public Color getBlockColor()
	{
		return blockColor;
	}
}
