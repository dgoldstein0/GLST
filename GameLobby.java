import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.GroupLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class GameLobby extends JDialog implements ActionListener, WindowListener, MouseListener
{
	JButton start_game;
	JButton leave_game;
	JButton choose_map;
	JFileChooser filechooser;
	JLabel map_label;
	JLabel[] player_names;
	JPanel[] color_samples;
	Integer[] color_nums;
	JFrame frame;
	
	GameControl GC;
	
	public GameLobby(JFrame f, GameControl gc)
	{
		super(f, "New Game", false);
		
		frame = f;
		GC=gc;

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		JPanel main_panel=new JPanel(new BorderLayout());
		//main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
		
		JPanel map_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));//use flowLayout to right-align the row
		
		map_label = new JLabel("No map selected"); //BOOKMARK!  should check with GC to see if map_file is already choosen.   
		map_panel.add(map_label);
		
		choose_map = new JButton("Select Map");
		choose_map.addActionListener(this);
		choose_map.setEnabled(GC.hosting);
		map_panel.add(choose_map);
		
		main_panel.add(map_panel, BorderLayout.NORTH);
		
		//Create and populate the panel.
		JPanel panel = new JPanel();
		
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		JLabel[] player_nums = new JLabel[GC.players.length];
		player_names = new JLabel[GC.players.length];
		color_samples =  new JPanel[GC.players.length];
		color_nums = new Integer[GC.players.length];
		
		GroupLayout.ParallelGroup hgroup1 = gl.createParallelGroup();
		GroupLayout.ParallelGroup hgroup2 = gl.createParallelGroup();
		GroupLayout.ParallelGroup hgroup3 = gl.createParallelGroup();
		GroupLayout.SequentialGroup vgroup = gl.createSequentialGroup();
		
		//create the header
		JLabel num = new JLabel("#");
		JLabel name = new JLabel("Name");
		JLabel color = new JLabel("Color");
		vgroup.addGroup(gl.createParallelGroup().addComponent(num).addComponent(name).addComponent(color));
		hgroup1.addComponent(num);
		hgroup2.addComponent(name);
		hgroup3.addComponent(color);
		
		for(int i=0; i< GC.players.length; i++)
		{	
			player_nums[i] = new JLabel(Integer.toString(i+1));
			player_nums[i].setFont(player_nums[i].getFont().deriveFont(Font.PLAIN));
			hgroup1.addComponent(player_nums[i]);
			
			player_names[i] = new JLabel();
			color_samples[i] = new JPanel();
			if(GC.players[i] instanceof Player){
				player_names[i].setText(GC.players[i].getName());
				color_samples[i].setBackground(GC.players[i].getColor()); //player.getColor() returns the default color here, since color is not yet set
				color_nums[i] = i;
			}
			player_names[i].setFont(player_names[i].getFont().deriveFont(Font.PLAIN));
			color_samples[i].setMinimumSize(new Dimension(50,15));
			color_samples[i].setPreferredSize(new Dimension(50,15));
			color_samples[i].setMaximumSize(new Dimension(50,15));
			color_samples[i].addMouseListener(this);
			
			hgroup2.addComponent(player_names[i]);
			hgroup3.addComponent(color_samples[i]);
			vgroup.addGroup(gl.createParallelGroup().addComponent(player_nums[i]).addComponent(player_names[i]).addComponent(color_samples[i]));
		}
		
		gl.setHorizontalGroup(gl.createSequentialGroup().addGroup(hgroup1).addGroup(hgroup2).addGroup(hgroup3));
		gl.setVerticalGroup(vgroup);
		
		JScrollPane scroller = new JScrollPane(panel);
		main_panel.add(scroller, BorderLayout.CENTER);
		
		JPanel p2 = new JPanel();
		
		start_game = new JButton("Start Game");
		start_game.addActionListener(this);
		start_game.setMnemonic(KeyEvent.VK_S);
		p2.add(start_game);
		
		leave_game = new JButton("Leave Game");
		leave_game.addActionListener(this);
		leave_game.setMnemonic(KeyEvent.VK_L);
		p2.add(leave_game);
		
		main_panel.add(p2, BorderLayout.SOUTH);
		
		add(main_panel);
		
		pack();
		setVisible(true);
		
		filechooser = new JFileChooser();
		filechooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));
	}
	
	public void updateNames()
	{
		for(int i=0; i<GC.players.length; i++){
			if(GC.players[i] instanceof Player){
				player_names[i].setText(GC.players[i].getName());
				if(!(color_nums[i] instanceof Integer)){
					color_samples[i].setBackground(GC.players[i].getColor()); //player.getColor() returns the default color here, since color is not yet set
					color_nums[i]=i;
				}
			} else {
				player_names[i].setText("");
				color_nums[i]=null;
				color_samples[i].setBackground(new Color(255,255,255,0));
			}
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==start_game)
		{
		}
		else if(e.getSource()==leave_game)
		{
			leaveGame();
		}
		else if(e.getSource() == choose_map)
		{
			int val = filechooser.showOpenDialog(frame);
			if(val==JFileChooser.APPROVE_OPTION){
				File map_file = filechooser.getSelectedFile();
				
				//load the map.  notify if errors.  This is supposed to validate the map by attempting to load it
				
				try{
					GC.loadMap(map_file);
					//BOOKMARK - need to change text in map_label to the NAME of the map
					map_label.setText(GC.map.getName());
				} catch(FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(frame, "The file was not found.  Please choose another file.", "Error - File not Found", JOptionPane.ERROR_MESSAGE);
				} catch(ClassCastException cce) {
					JOptionPane.showMessageDialog(frame, "The file you have selected is not a map", "Class Casting Error", JOptionPane.ERROR_MESSAGE);
				} catch(NullPointerException npe) {
					JOptionPane.showMessageDialog(frame, "Map loading failed.  The selected file is not a valid map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		//BOOKMARK - need to validate whether or not we are ready to start the game.  based on the result, enable or disable start_game
	}
	
	public void leaveGame()
	{
		if(GC.hosting)
			GC.endHost();
		GC.leavingLobby();
		
		GC.endConnection();
		dispose();
		GC.startupDialog();
	}
	
	//mouselistener code.  used for the color samples.
	public void mouseClicked(MouseEvent e){
		for(int i=0; i<color_samples.length; i++){
			if(e.getSource() == color_samples[i]){
				if(GC.players[i] instanceof Player){
					if(e.getButton() == MouseEvent.BUTTON1){
						color_nums[i]++;
						if(color_nums[i] >= GalacticStrategyConstants.DEFAULT_COLORS.length)
							color_nums[i]=0;
					}
					else if(e.getButton() == MouseEvent.BUTTON3){
						color_nums[i]--;
						if(color_nums[i] <0)
							color_nums[i]=GalacticStrategyConstants.DEFAULT_COLORS.length-1;
					}
					GC.players[i].setColor(GalacticStrategyConstants.DEFAULT_COLORS[color_nums[i]]);
					color_samples[i].setBackground(GC.players[i].getColor());
				}
			}
		}
	}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	//window listener used to check if you want to save your file when frame's X is clicked
	public void windowClosing(WindowEvent e){leaveGame();}
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}