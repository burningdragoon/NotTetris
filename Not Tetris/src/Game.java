import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;


public class Game extends JFrame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int DEFAULT_FPS = 10;
	
	private GamePanel gamePanel;
	
	public Game(long period)
	{
		super("Game");
		
		Container c = getContentPane();
		gamePanel = new GamePanel(this, period);
		c.setPreferredSize(gamePanel.getPreferredSize());
		this.setPreferredSize(gamePanel.getPreferredSize());
		c.add(gamePanel, "Center");
		
		addWindowListener(this);
		pack();
		setResizable(false);
		setVisible(true);
	}
	
	// -----------------------
	
	public void windowActivated(WindowEvent e) 
	{ gamePanel.resumeGame();  }

	public void windowDeactivated(WindowEvent e) 
	{ gamePanel.pauseGame();  }


	public void windowDeiconified(WindowEvent e) 
	{  gamePanel.resumeGame();  }

	public void windowIconified(WindowEvent e) 
	{  gamePanel.pauseGame(); }


	public void windowClosing(WindowEvent e)
	{  gamePanel.stopGame();  }
	
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	// ------------------------
	
	public static void main(String args[])
	{
		long period = (long) 1000.0/DEFAULT_FPS;
		new Game(period*100000L);
	}
}
