//import java.awt.event.WindowListener;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class InitializeGameTester
{
	GameControl GC;
	JFrame frame;
	
	public static void main(String[] args)
	{
		new InitializeGameTester();
	}
	
	public InitializeGameTester()
	{
		frame = new JFrame("Initialize Tester");
		
		JLabel label = new JLabel("testing... 1, 2, 3, testing...");
		label.setMinimumSize(new Dimension(500,500));
		label.setPreferredSize(new Dimension(500,500));
		frame.add(label);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//frame.addWindowListener(this);
		frame.pack();
		frame.setVisible(true);
		
		GC=new GameControl(frame);
		GC.startupDialog();
	}
		
	//window listener used to check if you want to save your file when frame's X is clicked
	/*public void windowClosing(WindowEvent e)
	{
		exitProgram();
	}
	
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}*/
}