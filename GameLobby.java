import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

public class GameLobby extends JDialog implements ActionListener, WindowListener
{
	JButton start_game;
	JButton leave_game;
	//JFileChooser filechooser;
	JPanel panel;
	
	GameControl GC;
	
	public GameLobby(JFrame frame, GameControl gc)
	{
		super(frame, "New Game", true);
		
		GC=gc;

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		JPanel p0=new JPanel();
		p0.setLayout(new BoxLayout(p0, BoxLayout.Y_AXIS));
		
		panel = new JPanel(new GridLayout(1,2));//GalacticStrategyConstants.MAX_PLAYERS+
		panel.add(new JLabel(Integer.toString(GC.the_player.getId())));
		panel.add(new JLabel(GC.the_player.getName()));
		p0.add(panel);
		
		JPanel p2 = new JPanel();
		
		start_game = new JButton("Start Game");
		start_game.addActionListener(this);
		start_game.setMnemonic(KeyEvent.VK_S);
		p2.add(start_game);
		
		leave_game = new JButton("Leave Game");
		leave_game.addActionListener(this);
		leave_game.setMnemonic(KeyEvent.VK_L);
		p2.add(leave_game);
		
		p0.add(p2);
		
		add(p0);
		
		pack();
		setVisible(true);
		//nothing down here.  dialog is modal, so setVisible hangs execution on the thread calling this
	}
	
	public void refreshPlayers()
	{
		panel.removeAll();
		
		panel.add(new JLabel(Integer.toString(GC.the_player.getId())));
		panel.add(new JLabel(GC.the_player.getName()));
		
		for(Player p : GC.players)
		{
			panel.add(new JLabel(Integer.toString(p.getId())));
			panel.add(new JLabel(p.getName()));
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==start_game)
		{
		}
		else if(e.getSource()==leave_game)
		{
			leaveGame(true);
		}
	}
	
	public void leaveGame(boolean force_close)
	{
		if(GC.hosting)
			GC.endHost();
		
		GC.endConnection();
		/*if(force_close)
			SwingUtilities.invokeLater(new Runnable(){public void run(){dispose();}});*/
		dispose();
		GC.startupDialog();
	}
	
	//window listener used to check if you want to save your file when frame's X is clicked
	public void windowClosing(WindowEvent e){leaveGame(false);}
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}