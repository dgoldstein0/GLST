import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class GameStartupDialog implements ActionListener
{
	JButton o_screen_host;
	JButton o_screen_join;
	JButton o_screen_test;
	JButton o_screen_about;
	JButton o_screen_exit;
	JDialog open_screen_dialog;
	GameControl GC;
	JFrame frame;
	
	public GameStartupDialog(JFrame f, GameControl gc)
	{
		frame=f;
		GC = gc;
		
		o_screen_host = new JButton("Host Game");
		o_screen_host.setMnemonic(KeyEvent.VK_H);
		o_screen_host.addActionListener(this);
		
		o_screen_join = new JButton("Join Game");
		o_screen_join.setMnemonic(KeyEvent.VK_J);
		o_screen_join.addActionListener(this);
		
		o_screen_test = new JButton("Single Player Testing");
		o_screen_test.setMnemonic(KeyEvent.VK_S);
		o_screen_test.addActionListener(this);
		
		o_screen_about = new JButton("Help/About");
		o_screen_about.setMnemonic(KeyEvent.VK_A);
		o_screen_about.addActionListener(this);

		o_screen_exit = new JButton("Exit");
		o_screen_exit.setMnemonic(KeyEvent.VK_X);
		o_screen_exit.addActionListener(this);
		
		constructDialog();
	}
	
	public void constructDialog()
	{
		open_screen_dialog = new JDialog(frame, "Galactic Strategy Game Start", true);
		open_screen_dialog.setLayout(new GridLayout(5,1));
		
		open_screen_dialog.add(o_screen_host);
		open_screen_dialog.add(o_screen_join);
		open_screen_dialog.add(o_screen_test);
		open_screen_dialog.add(o_screen_about);
		open_screen_dialog.add(o_screen_exit);
		
		open_screen_dialog.pack();
		
		open_screen_dialog.setLocation(new Point((frame.getWidth()-open_screen_dialog.getWidth())/2, (frame.getHeight()-open_screen_dialog.getHeight())/2)); //center the dialog
		if(!open_screen_dialog.isVisible())
			open_screen_dialog.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == o_screen_host)
		{
			open_screen_dialog.dispose();
			GC.host(); //launches a model dialog, hence the order of commands
		}
		else if(e.getSource() == o_screen_join)
		{
			open_screen_dialog.dispose();
			GC.joinAsClient();
		}
		else if(e.getSource() == o_screen_about)
		{
		}
		else if(e.getSource() == o_screen_exit)
		{
			System.exit(0);
		}
		else if(e.getSource() == o_screen_test)
		{
			open_screen_dialog.dispose();
			GC.startTest(0, false, null);
		}
	}
}