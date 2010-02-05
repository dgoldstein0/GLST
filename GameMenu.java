import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GameMenu extends JDialog implements ActionListener
{
	JFrame frame;
	
	public GameMenu(JFrame f)
	{
		super(f, "Menu");
		frame = f;
		//set up the menu here
		
		
		pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	}
	
	public void showMenu()
	{
		setLocation((frame.getWidth()-getWidth())/2, (frame.getHeight()-getHeight())/2);
		setVisible(true);
	}
}