import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;

public class GameMenu extends JDialog implements ActionListener
{
	JFrame frame;
	GameControl GC;
	
	JButton pause;
	JButton save;
	JButton settings;
	JButton quit_game;
	JButton exit;
	JButton resume_game;
	
	public GameMenu(GameControl g, JFrame f)
	{
		super(f, "Menu", true);
		frame = f;
		GC =g;
		//set up the menu here
		setLayout(new GridLayout(6,1));
		
		//Pause game
		pause = new JButton("Pause Game");
		pause.addActionListener(this);
		add(pause);
		
		//save game
		save = new JButton("Save Game");
		save.addActionListener(this);
		add(save);
		
		//game settings
		settings = new JButton("Settings...");
		settings.addActionListener(this);
		add(settings);
		
		//quit game button
		quit_game = new JButton("Quit Game");
		quit_game.addActionListener(this);
		add(quit_game);
		
		//quit and exit button
		exit = new JButton("Exit Program");
		exit.addActionListener(this);
		add(exit);
		
		//return to game/cancel
		resume_game = new JButton("Resume");
		resume_game.addActionListener(this);
		add(resume_game);
		
		pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == pause)
			JOptionPane.showMessageDialog(this, "this hasn't been implemented yet.", "Error", JOptionPane.ERROR_MESSAGE);
		else if(e.getSource() == save)
			JOptionPane.showMessageDialog(this, "this hasn't been implemented yet.", "Error", JOptionPane.ERROR_MESSAGE);
		else if(e.getSource() == settings)
			JOptionPane.showMessageDialog(this, "this hasn't been implemented yet.", "Error", JOptionPane.ERROR_MESSAGE);
		else if(e.getSource() == quit_game)
		{
			GC.endAllThreads();
			GC.players = new Player[GalacticStrategyConstants.MAX_PLAYERS];
			setVisible(false); //hide the menu
			//destroy the graphics.
			GC.GI.graphics_started=false;
			GC.GI.sat_or_ship_disp = GameInterface.PANEL_DISP.NONE;
			GC.GI.theinterface.removeAll(); //removes the system/galaxy display
			GC.GI.theinterface.repaint();
			GC.GI.stat_and_order.removeAll();
			GC.GI.stat_and_order.repaint();
			GC.GI.system_list.removeAll();
			GC.GI.system_list.repaint();
			GC.GI.selected_sys.clear();
			GC.GI.selected_in_sys.clear();;
			GC.GI.labels_made=false;
			GC.GI.prev_sys=null;
			GC.GI.prev_selected=null;
			frame.setVisible(true);
			GC.startupDialog();
		}
		else if(e.getSource() == exit)
		{
			//this kills the program
			GC.endAllThreads();
			frame.dispose();
		}
		else if(e.getSource() == resume_game)
			setVisible(false);
	}
	
	public void showMenu()
	{
		pack(); //fixes sizing issues, in case the user resized the menu
		setLocation((frame.getWidth()-getWidth())/2, (frame.getHeight()-getHeight())/2);
		setVisible(true);
	}
}