package galactic_strategy.ui;

import galactic_strategy.Constants;
import galactic_strategy.GameControl;
import galactic_strategy.Player;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;

public class GameMenu extends JDialog implements ActionListener
{
	private static final long serialVersionUID = -4940203546006948917L;
	
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
			GC.setPlayers(new Player[Constants.MAX_PLAYERS]);
			setVisible(false); //hide the menu
			//destroy the graphics.
			GC.GI.reset();
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