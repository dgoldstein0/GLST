import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.GroupLayout;
import javax.swing.SwingUtilities;
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
	static final String LOAD_MAP_MSG = "No map selected";
	
	JButton start_game;
	JButton leave_game;
	JButton choose_map;
	JFileChooser filechooser;
	JLabel map_label;
	JLabel[] player_names;
	JPanel[] color_samples;
	Integer[] color_nums;
	JFrame frame;
	
	boolean is_hosting;
	
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
		
		map_label = new JLabel(LOAD_MAP_MSG); //BOOKMARK!  should check with GC to see if map_file is already choosen.   
		map_panel.add(map_label);
		
		is_hosting = GC.players[GC.player_id].hosting;
		
		choose_map = new JButton("Select Map");
		choose_map.addActionListener(this);
		choose_map.setEnabled(is_hosting);
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
			if(GC.players[i] != null){
				player_names[i].setText(GC.players[i].getName());
				if(GC.players[i].ready)
					player_names[i].setForeground(Color.GREEN);
				else
					player_names[i].setForeground(Color.RED);
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
		
		if(is_hosting)
			start_game = new JButton("Start Game");
		else
			start_game = new JButton("Ready!");
		start_game.addActionListener(this);
		start_game.setMnemonic(KeyEvent.VK_S);
		start_game.setEnabled(false);
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
			if(GC.players[i] != null){
				player_names[i].setText(GC.players[i].getName());
				if(!(color_nums[i] != null)){
					color_samples[i].setBackground(GC.players[i].getColor()); //player.getColor() returns the default color here, since color is not yet set
					color_nums[i]=i;
				}
				if(GC.players[i].ready)
					player_names[i].setForeground(Color.GREEN);
				else
					player_names[i].setForeground(Color.RED);
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
			if(is_hosting)
			{
				GC.startGameViaThread();
				//dispose();
			}	
			else {
				GC.declareReady();
				updateNames();
			}
		}
		else if(e.getSource()==leave_game)
		{
			leaveGame(true);
		}
		else if(e.getSource() == choose_map)
		{
			int val = filechooser.showOpenDialog(frame);
			if(val==JFileChooser.APPROVE_OPTION){
				File map_file = filechooser.getSelectedFile();
				
				//load the map.  notify if errors.  This is supposed to validate the map by attempting to load it
				
				try{
					try{
						GC.loadMap(map_file); //parsing errors render the map invalid, causing one of the messages in the catch statements.
					} catch(GameControl.MapLoadException mle)
					{
						JOptionPane.showMessageDialog(frame,	"The map was loaded, but errors occured during the load,\n" +
																"which likely mean that the map is not compatible with this\n" +
																"version of the game.  It is recommended that you choose a\n" +
																"different map.", "Map Load Exception", JOptionPane.ERROR_MESSAGE);
					}
					
					//the existence of the name is the second line of defense.
					if(GC.map.getName() != null){
						map_label.setText(GC.map.getName());
					}
					else{
						GC.map=null;
						map_label.setText(LOAD_MAP_MSG);
						JOptionPane.showMessageDialog(frame, "The selected file is not a completed map.  Please pick a different map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch(FileNotFoundException fnfe) {
					map_label.setText(LOAD_MAP_MSG); //just in case switching from valid to invalid map
					JOptionPane.showMessageDialog(frame, "The file was not found.  Please choose another file.", "Error - File not Found", JOptionPane.ERROR_MESSAGE);
				} catch(ClassCastException cce) {
					map_label.setText(LOAD_MAP_MSG); //just in case switching from valid to invalid map
					JOptionPane.showMessageDialog(frame, "The file you have selected is not a map", "Class Casting Error", JOptionPane.ERROR_MESSAGE);
				} catch(NullPointerException npe) {
					map_label.setText(LOAD_MAP_MSG); //just in case switching from valid to invalid map
					JOptionPane.showMessageDialog(frame, "Map loading failed.  The selected file is not a valid map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
				}
				//notify other players - send the text in map_label
				GC.mapChosen();
				start_game.setEnabled(readyToStart()); //after chosing a valid map, the host could choose an invalid one, thus making the game not ready to start.  For this reason, the setEnabled of start_game belongs down here
			}
		}
		
		//BOOKMARK - need to validate whether or not we are ready to start the game.  based on the result, enable or disable start_game
	}
	
	public void leaveGame(boolean swing_thread)
	{
		if(is_hosting)
			GC.endHost();
		GC.leavingLobby();
		
		if(GC.startThread != null)
			GC.startThread.interrupt();
		
		GC.endConnection();
		if(swing_thread)//does this always run on the event thread?  need to double check
			dispose();
		else
			SwingUtilities.invokeLater(new Runnable(){public void run(){dispose();}});
		GC.startupDialog();
	}
	
	public boolean readyToStart(){
		System.out.println("check ready to start");
		if(GC.getNumberOfPlayers()>=2) {
			if(!map_label.getText().equals(LOAD_MAP_MSG)){
				
				//check colors
				for(int i=0; i<GC.players.length; i++){
					if(color_nums[i] != null){ 
						for(int j=i+1; j<GC.players.length; j++){
							if(color_nums[j] != null){
								if(color_nums[i].equals(color_nums[j]))
									return false; //two colors match
							}
						}
						if(i != 0 && is_hosting && !GC.players[i].ready)//color_nums[i] instanceof Integer iff players[i] instanceof Player... else error here...
							return false; //host can't start until all clients are ready
					}
				}
				return true;
			} else
				return false;
		} else {
			return false;
		}
	}
	
	//mouselistener code.  used for the color samples.
	public void mouseClicked(MouseEvent e){
		if(!GC.players[GC.player_id].ready){ //once ready is clicked by a client, the colors cannot be altered
			for(int i=0; i<color_samples.length; i++){
				if(e.getSource() == color_samples[i]){
					if(GC.players[i] != null){
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
			start_game.setEnabled(readyToStart());
		}
	}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	//window listener used to check if you want to save your file when frame's X is clicked
	public void windowClosing(WindowEvent e){leaveGame(true);}
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}