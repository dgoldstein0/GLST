import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.Point;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.util.*;


public class GameInterface implements ActionListener, MouseListener, WindowListener, ComponentListener, MouseWheelListener, AWTEventListener
{
	
	JFrame frame;
	JPanel panel,topbar;
	JLabel time,metal,money;	
	JButton menubutton;
	GameMenu menu;
	JTabbedPane tabbedPane;
	JScrollPane pane1;
	JScrollPane pane2;
	boolean labels_made=false; //check if the labels have been made 
	JTextArea log;
	JPanel stat_and_order;
	JPanel theinterface;
	
	ArrayList<GSystem> known_sys; //known systems for this player
	ArrayList<Satellite> known_sate;   //known satellite for this player
	static GameControl GC;
	
	GalacticMapPainter GalaxyPanel;
	double gal_scale; //the scale which the galaxy is painted at.
	HashSet<GSystem> selected_sys; //stores a set of currently selected systems - that is, a set of one item.  This is necessary because multiple selection is possible in GDFrame
	
	
	SystemPainter SystemPanel;
	GSystem sys,prev_sys; //doubles as the variable of the selected system in Galaxy as as the currently open system
	Selectable selected_in_sys, prev_selected;
	double prev_scale,prev_x,prev_y;
	double sys_scale;
	double sys_center_x;
	double sys_center_y;
	double move_center_x_speed = 0; //left is negqtive, right is positive
	double move_center_y_speed = 0; //up is negative, down is positive
	MoveScreenCursors cursors;
	int recenter_delay;
	long last_time_recentered;
	
	int system_state;
		final static int NORMAL=0;
		final static int SELECT_DESTINATION=1;
		
	static int EDGE_BOUND=20; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static int SYS_WIDTH=GalacticStrategyConstants.SYS_WIDTH; //the allowed width of a system
	static int SYS_HEIGHT=GalacticStrategyConstants.SYS_HEIGHT; //the allowed height of a system
	
	JPanel system_list;
	JPanel satellites_list;
	
	int sat_or_ship_disp;
		static final int SAT_PANEL_DISP = 0;
		static final int SHIP_PANEL_DISP=1;
		static final int NO_PANEL_DISP = -1;
	PlanetMoonCommandPanel SatellitePanel;
	ShipCommandPanel ShipPanel;
	
	boolean mode, prev_mode; //Galaxy=true, system=false.  reflected by isGalaxyDisplayed and isSystemDisplayed
	boolean graphics_started; //used to indicate whether graphics have been started yet - that is, whether the Galaxy has been drawn yet.
	
	final String indentation="     ";


	public GameInterface()
	{
		//create frame and layout	
		frame=new JFrame("Galactic Strategy Game");
		//frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		//frame.setSize(1500,900);
		frame.setMinimumSize(new Dimension(800,600));
		frame.addWindowListener(this);
		frame.addComponentListener(this);
		
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK);
		
		panel= new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		frame.add(panel);
		
		//create topbar
		topbar=new JPanel(new GridBagLayout());
		//create money	
		money=new JLabel(indentation+"Money: 0");
	//	resource.setSize(600, 200);		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =0.4;
		c.weighty=0; 
		c.gridx = 0;
		c.gridy = 0;
		//c.gridwidth=2;
		c.anchor=GridBagConstraints.NORTH;
		c.gridheight=1;
		topbar.add(money,c);
						
		//create metal

		metal=new JLabel(indentation+"Metal: 0");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =0.4;
		c.weighty=0; 
		c.gridx = 5;
		c.gridy = 0;
		c.anchor=GridBagConstraints.NORTH;
		c.gridheight=1;
		topbar.add(metal,c);
		
		//create time
		time=new JLabel("Time: 0");
	//	time.setSize(600, 200);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =0.4;
		c.weighty=0;
		c.gridx = 10;
		c.gridy = 0;
		//c.gridwidth=2;
		topbar.add(time,c);
		
		//add topbar
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.weightx=1;
		c.weighty=0.0;		
		c.gridx=0;
		c.gridy=0;
		topbar.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(topbar,c);
		

		//Create the menu button
		menubutton=new JButton("menu");
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				menu.showMenu();
			}
		};
		menubutton.addActionListener(actionListener);
		menubutton.setSize(300, 100);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.weightx =0.41;
		c.gridx = 15;
		c.gridy = 0;    
		topbar.add(menubutton,c);
		

		
		//create the tabbed pane
		tabbedPane = new JTabbedPane();
		//tabbedPane.setSize(200, 700);
		system_list = new JPanel();//makeTextPanel("System");
		system_list.setLayout(new BoxLayout(system_list,BoxLayout.Y_AXIS));
		pane1 = new JScrollPane(system_list);		
		pane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("System", pane1);
		tabbedPane.setSelectedIndex(0);
		satellites_list = new JPanel();//makeTextPanel("Planets");
		satellites_list.setLayout(new BoxLayout(satellites_list,BoxLayout.Y_AXIS));
		
		pane2 = new JScrollPane(satellites_list);		
		pane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Planets", pane2);
		
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.EAST;
		c.weightx =0;
		c.weighty=0.8;
		c.gridwidth=5;
		c.gridx = 15;
		c.gridy = 1;    
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(tabbedPane,c);


		//create the interface
		theinterface = new JPanel(new BorderLayout());
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.CENTER;
		c.weightx =0.5;
		c.weighty=0.5;
		c.gridx = 0;
		c.gridy = 1;    
		theinterface.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		theinterface.addMouseListener(this);
		theinterface.addMouseWheelListener(this);
		panel.add(theinterface,c);
		
		//create the chat and information log
		log=new JTextArea("log");
		JScrollPane scrollPane = new JScrollPane(log);
		scrollPane.setPreferredSize(new Dimension(200, 140));		
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.SOUTHEAST;
		c.weightx =0; //was .2
		c.weighty=0;//was .2
		c.gridx = 15;
		c.gridy = 2;   
		panel.add(scrollPane,c);
		
		//create the stat and order panel
		stat_and_order=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		c.fill=GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.SOUTHWEST;
		c.weighty=0;
		c.weightx=1;
		c.gridx=0;
		c.gridy=2;
		stat_and_order.setBorder(BorderFactory.createLineBorder(Color.RED));
		panel.add(stat_and_order,c);	
	
		selected_sys = new HashSet<GSystem>();
	
		frame.pack();
		
		//set up game control
		GC = new GameControl(this);
		
		system_state = NORMAL;
		setupGraphics();
		
		//set up in-game menu for later display
		menu=new GameMenu(GC, frame);
		//set up cursors for later use
		cursors = new MoveScreenCursors();
		
		sys_scale = 1.0d;
		sys_center_x = theinterface.getWidth()/2;
		sys_center_y = theinterface.getHeight()/2;
		
		frame.setVisible(true);	
		GC.startupDialog();
	}
	

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable(){public void run(){new GameInterface();}});
	}
	
	private void setupGraphics()
	{
		//sets up GalacticMapPainter, SystemPainter
		GalaxyPanel = new GalacticMapPainter();
		SystemPanel = new SystemPainter(false);
		
		SatellitePanel = new PlanetMoonCommandPanel();
		ShipPanel = new ShipCommandPanel();
		
		graphics_started=false;
		sat_or_ship_disp = NO_PANEL_DISP;
	}
	
	public void drawGalaxy()
	{
		System.out.println("Draw Galaxy!");
		//this method shows the GalacticMapPainter in the main viewspace
		if(isSystemDisplayed() || !graphics_started)
		{
			theinterface.removeAll();
			theinterface.add(GalaxyPanel); //automatically adds to center
			mode=true;
			prev_mode=true;
			graphics_started=true;
			
			//make sure to change cursor back to normal
			theinterface.setCursor(Cursor.getDefaultCursor());
			selected_in_sys=null;
			displayNoPanel();
		}
		gal_scale =  Math.min(((double)theinterface.getWidth())/((double)GalacticStrategyConstants.GALAXY_WIDTH), ((double)theinterface.getHeight())/((double)GalacticStrategyConstants.GALAXY_HEIGHT));
		GalaxyPanel.paintGalaxy(GC.map, selected_sys, GDFrame.DRAG_NONE, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, gal_scale);
		frame.setVisible(true); //makes all components within the frame displayable.  frame.pack() does this too, but pack resizes the frame to fit all components in their preferred sizes
	}
	
	
	//before calling this function, sys should be specified
	//note that this function makes selected_in_sys null if it is used to switch from the galaxy to a system
	public void drawSystem()
	{
		System.out.println("Draw System!");
		//this method shows the GalacticMapPainter in the main viewspace
		if(isGalaxyDisplayed())
		{
			theinterface.removeAll();
			theinterface.add(SystemPanel); //automatically adds to center
			mode=false;
		}
/*		sys_scale = 1.0d;
		sys_center_x = theinterface.getWidth()/2;
		sys_center_y = theinterface.getHeight()/2;*/
		SystemPanel.paintSystem(sys, selected_in_sys, sys_center_x, sys_center_y, sys_scale, true);
		frame.setVisible(true); //makes all components within the frame displayable.  frame.pack() does this too, but pack resizes the frame to fit all components in their preferred sizes
	}
	
	public void redraw()
	{
		if(graphics_started)
		{
			if(isGalaxyDisplayed())
			{
				gal_scale =  Math.min(((double)theinterface.getWidth())/((double)GalacticStrategyConstants.GALAXY_WIDTH), ((double)theinterface.getHeight())/((double)GalacticStrategyConstants.GALAXY_HEIGHT));
				//System.out.println(Double.toString(gal_scale));
				GalaxyPanel.paintGalaxy(GC.map, selected_sys, GDFrame.DRAG_NONE, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, gal_scale);
			}
			else //before getting to here, sys and selected_in_sys should be specified.  the latter may be null.
			{
				SystemPanel.paintSystem(sys, selected_in_sys, sys_center_x, sys_center_y, sys_scale, true);
			}
		}
	}
	
	public void displaySatellitePanel(Satellite s)
	{
		SatellitePanel.setSat(s);
		
		if(sat_or_ship_disp != SAT_PANEL_DISP)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			stat_and_order.add(SatellitePanel);
			sat_or_ship_disp = SAT_PANEL_DISP;
		}
		
		frame.setVisible(true);
	}
	
	public void displayShipPanel(Ship s)
	{
		if(sat_or_ship_disp != SHIP_PANEL_DISP)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			stat_and_order.add(ShipPanel);
			sat_or_ship_disp = SHIP_PANEL_DISP;
		}
		
		ShipPanel.setShip(s);
		
		frame.setVisible(true);
	}
	
	public void displayNoPanel()
	{
		if(sat_or_ship_disp != NO_PANEL_DISP)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			sat_or_ship_disp = NO_PANEL_DISP;
			SatellitePanel.state=-1; //this is necessary to remove the bug
				//that when a facility finishes being built, and the player has nothing in the system selected,
				//and then selects a planet with no facilities, the newly built facility appears in the interface
				//as if it belongs to the selected planet, although the facility is not part of that planet's (or moon's)
				//facilities ArrayList.  setting this boolean to false ensures that displayFacility in PlanetMoonCommandPanel
				//does not try to add the new facility to the class' facilities_panel.
			
			frame.setVisible(true);
		}
	}
	
	public void switchSystemToDestinationMode()
	{
		system_state = SELECT_DESTINATION;
	}
	
	public boolean isGalaxyDisplayed(){return mode;}
	public boolean isSystemDisplayed(){return !mode;}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	//this is ONLY invoked by MOUSE MOTION EVENTS, and is in charge of deciding how fast to move the view of the system
	public void eventDispatched(AWTEvent a)
	{
		MouseEvent e=(MouseEvent)a;
		int x, y;
		JComponent c;
		
		Point corner;
		if(e.getSource() instanceof JComponent)
			corner = ((JComponent)e.getSource()).getLocationOnScreen();
		else
			corner = frame.getLocationOnScreen();
		
		Point pane_pos = frame.getContentPane().getLocationOnScreen();
		x=e.getX() - pane_pos.x + corner.x;
		y=e.getY() - pane_pos.y + corner.y;
		
		//System.out.println("x,y is " + Integer.toString(x) + "," + Integer.toString(y));
		
		if(isSystemDisplayed())
		{
			//boolean previously_moving = true;
			//if(move_center_x_speed==0.0 && move_center_y_speed==0.0)
			//	previously_moving = false;
			
			//sets the speed for camera motion
			if(EDGE_BOUND<x && x<frame.getContentPane().getWidth()-EDGE_BOUND)
				move_center_x_speed=0.0;
			else if(x>frame.getContentPane().getWidth()-EDGE_BOUND && x<=frame.getContentPane().getWidth())
			{
				if(x>frame.getContentPane().getWidth()-EDGE_BOUND/2)
					move_center_x_speed = 1.0/sys_scale;
				else
					move_center_x_speed=.5/sys_scale;
			}
			else if(x< EDGE_BOUND && x >=0)
			{
				if(x< EDGE_BOUND/2)
					move_center_x_speed = -1.0/sys_scale;
				else
					move_center_x_speed= -.5/sys_scale;
			}

			if(EDGE_BOUND<y && y<frame.getContentPane().getHeight()-EDGE_BOUND)
				move_center_y_speed=0.0;
			else if(y>frame.getContentPane().getHeight()-EDGE_BOUND && y<=frame.getContentPane().getHeight())
			{
				if(y>frame.getContentPane().getHeight()-EDGE_BOUND/2)
					move_center_y_speed = 1.0/sys_scale;
				else
					move_center_y_speed = .5/sys_scale;
			}
			else if(y< EDGE_BOUND && y >=0)
			{
				if(y < EDGE_BOUND/2)
					move_center_y_speed = -1.0/sys_scale;
				else
					move_center_y_speed = -.5/sys_scale;
			}
			
			//else if(!previously_moving)
			//	recenter_delay=300; //the delay which to wait before moving the screen
			
			//Set the cursor
			boolean move_screen = true;
			if(move_center_x_speed > 0.0){
				if(move_center_y_speed > 0.0)
					frame.getGlassPane().setCursor(cursors.downRight());
				else if(move_center_y_speed < 0.0)
					frame.getGlassPane().setCursor(cursors.upRight());
				else
					frame.getGlassPane().setCursor(cursors.right());
			}
			else if(move_center_x_speed < 0.0){
				if(move_center_y_speed > 0.0)
					frame.getGlassPane().setCursor(cursors.downLeft());
				else if(move_center_y_speed < 0.0)
					frame.getGlassPane().setCursor(cursors.upLeft());
				else
					frame.getGlassPane().setCursor(cursors.left());
			}
			else{ //if move_center_x_speed equals 0
				if(move_center_y_speed > 0.0)
					frame.getGlassPane().setCursor(cursors.down());
				else if(move_center_y_speed < 0.0)
					frame.getGlassPane().setCursor(cursors.up());
				else
					move_screen=false;
			}
			
			frame.getGlassPane().setVisible(move_screen);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if(isSystemDisplayed())
		{
			move_center_x_speed=0;
			move_center_y_speed=0;
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if(graphics_started) {
			if(isGalaxyDisplayed()){
				selectSystemAt(((double)e.getX())/gal_scale, ((double)e.getY())/gal_scale);
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && sys instanceof GSystem)
					drawSystem();
				else
					redraw();
			} else { //system is displayed
				if(e.getX() >= theinterface.getWidth() - SystemPainter.arrow_size && e.getY() <= SystemPainter.arrow_size)
					drawGalaxy();
				else
				{
					if(system_state == NORMAL && e.getButton() == MouseEvent.BUTTON1)
					{
						//look for object to select
						selectInSystemAt(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()));
						redraw();
					}
					else if(system_state == SELECT_DESTINATION)
					{
						setDestination(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()));
						system_state = NORMAL;
					}
					else if(selected_in_sys instanceof Ship && e.getButton() == MouseEvent.BUTTON3)
					{
						setDestination(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()));
					}
				}
			}
		}
	}
	
	private void selectSystemAt(double x, double y)
	{
		for(GSystem the_sys : GC.map.systems) //BOOKMARK - this should search through GC.players[GC.player_id].known_systems
		{
			if(Math.hypot(the_sys.x - x, the_sys.y-y) <= GalacticStrategyConstants.SELECTION_TOLERANCE)
			{
				sys=the_sys;
				selected_sys.clear();
				selected_sys.add(sys);
				return;
			}
		}
		
		//if no system found, make sure to deselect
		sys=null;
		selected_sys.clear();
	}
	
	private void selectInSystemAt(double x, double y)
	{
		final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/sys_scale; //tolerance
		
		if(sys.stars instanceof HashSet)
		{
			for(Star st : sys.stars)
			{
				//search for star...
				if(st.x-st.size/2 <= x && x <= st.x+st.size/2 && st.y-st.size/2 <= y && y <= st.y+st.size/2)
				{
					selected_in_sys = st;
					displayNoPanel();
					return;
				}
			}
		}
		
		//search orbiting planets/objects
		if(sys.orbiting instanceof ArrayList)
		{
			for(Satellite orbiting : sys.orbiting)
			{
				//search for satellites...
				if(orbiting.absoluteCurX()-orbiting.size/2 -OBJ_TOL <= x && x <= orbiting.absoluteCurX()+orbiting.size/2 + OBJ_TOL && orbiting.absoluteCurY()-orbiting.size/2-OBJ_TOL <= y && y <= orbiting.absoluteCurY() + orbiting.size/2+OBJ_TOL)
				{
					selected_in_sys=orbiting;
					displaySatellitePanel(orbiting);
					return;
				}
				
				if(orbiting instanceof Planet && ((Planet)(orbiting)).orbiting instanceof ArrayList)
				{
					Planet cur_planet=(Planet)orbiting;
					for(Satellite sat : cur_planet.orbiting)
					{
						if(sat.absoluteCurX()-sat.size/2 -OBJ_TOL <= x && x <= sat.absoluteCurX()+sat.size/2+OBJ_TOL && sat.absoluteCurY()-sat.size/2-OBJ_TOL <= y && y <= sat.absoluteCurY()+sat.size/2+OBJ_TOL)
						{
							selected_in_sys=sat;
							displaySatellitePanel(sat);
							return;
						}
					}
				}
			}
		}
		
		for(int i=0; i<sys.fleets.length; i++)
		{
			for(Integer j : sys.fleets[i].ships.keySet())
			{
				Ship s = sys.fleets[i].ships.get(j);
				
				if(s.pos_x-OBJ_TOL-s.type.dim*s.type.default_scale/2*sys_scale <= x && x <= s.pos_x+OBJ_TOL+s.type.dim*s.type.default_scale/2 && s.pos_y-OBJ_TOL-s.type.dim*s.type.default_scale/2 <= y && y <= s.pos_y+OBJ_TOL+s.type.dim*s.type.default_scale/2)
				{
					selected_in_sys = s;
					displayShipPanel(s);
					return;
				}
			}
		}
		
		//if nothing found
		selected_in_sys = null;
		displayNoPanel();
	}
	
	private void setDestination(double x, double y)
	{
		Destination dest = new DestinationPoint(x,y);
		
		//1st search out satellites
		
		final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/sys_scale; //tolerance
		
		//search orbiting planets/objects
		if(sys.orbiting instanceof ArrayList)
		{
			for(Satellite sat : sys.orbiting)
			{
				//search for satellites...
				if(sat.absoluteCurX()-sat.size/2 -OBJ_TOL <= x && x <= sat.absoluteCurX()+sat.size/2 + OBJ_TOL && sat.absoluteCurY()-sat.size/2-OBJ_TOL <= y && y <= sat.absoluteCurY() + sat.size/2+OBJ_TOL)
				{
					dest = sat;
					break;
				}
				
				if(sat instanceof Planet && ((Planet)(sat)).orbiting instanceof ArrayList)
				{
					Planet cur_planet=(Planet)sat;
					for(Satellite sat2 : cur_planet.orbiting)
					{
						if(sat2.absoluteCurX()-sat2.size/2 -OBJ_TOL <= x && x <= sat2.absoluteCurX()+sat2.size/2+OBJ_TOL && sat2.absoluteCurY()-sat2.size/2-OBJ_TOL <= y && y <= sat2.absoluteCurY()+sat2.size/2+OBJ_TOL)
						{
							dest=sat2;
							break;
						}
					}
				}
			}
		}
		
		for(int i=0; i<sys.fleets.length; i++)
		{
			for(Integer j : sys.fleets[i].ships.keySet())
			{
				Ship s = sys.fleets[i].ships.get(j);
				
				if(s.pos_x-OBJ_TOL-s.type.dim*s.type.default_scale/2*sys_scale <= x && x <= s.pos_x+OBJ_TOL+s.type.dim*s.type.default_scale/2 && s.pos_y-OBJ_TOL-s.type.dim*s.type.default_scale/2 <= y && y <= s.pos_y+OBJ_TOL+s.type.dim*s.type.default_scale/2)
				{
					dest = s;
					break;
				}
			}
		}
		Long cur_time = GameInterface.GC.TC.getTime();
		
		ShipPanel.the_ship.orderToMove(cur_time, dest);
		ShipPanel.updateDestDisplay();
		
		if(dest instanceof Ship)
		{			
			ShipPanel.the_ship.orderToAttack(cur_time,(Targetable)dest);
		}
	}
	
	public void update()      					//subject to change
	{		
		if (!labels_made)
		{
			System.out.println("made");
			known_sys=GC.map.systems;//GC.players[GC.player_id].known_systems;
			known_sate=GC.players[GC.player_id].known_satellites;		
			labels_made=true;									
			system_list.removeAll();		
			for (GSystem system :known_sys)
			{
				SystemLabel label=new SystemLabel(system,this);
				system_list.add(label);			
			}
		
		}
		moveCenter();
		frame.setVisible(true);
	}
	
	private void moveCenter()
	{
		long cur_time = GC.TC.getTime();
		double old_center_x = sys_center_x;
		double old_center_y = sys_center_y;
		if(recenter_delay == 0)
		{
			sys_center_x += move_center_x_speed*(cur_time-last_time_recentered);
			sys_center_y += move_center_y_speed*(cur_time-last_time_recentered);
		}
		else
		{
			recenter_delay -= cur_time-last_time_recentered;
			if(recenter_delay < 0)
			{
				sys_center_x -= move_center_x_speed*recenter_delay;
				sys_center_y -= move_center_y_speed*recenter_delay;
				recenter_delay=0;
			}
		}
		last_time_recentered = cur_time;
		
		if(sysScreenToDataX(0)<(theinterface.getWidth()-SYS_WIDTH)/2 || sysScreenToDataX(theinterface.getWidth())>(theinterface.getWidth()+SYS_WIDTH)/2)
			sys_center_x = old_center_x;
		
		if(sysScreenToDataY(0)<(theinterface.getHeight()-SYS_HEIGHT)/2 || sysScreenToDataY(theinterface.getHeight())>(theinterface.getHeight()+SYS_HEIGHT)/2)
			sys_center_y = old_center_y;
	}
	
	private double sysScreenToDataX(int x)
	{
		return (x-theinterface.getWidth()/2)/sys_scale+sys_center_x;
	}
	
	private double sysScreenToDataY(int y)
	{
		return (y-theinterface.getHeight()/2)/sys_scale+sys_center_y;
	}
	
	//used by systemPanel
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if(isSystemDisplayed())
		{
			sys_scale *= 1.0-((double)e.getWheelRotation())*GalacticStrategyConstants.SCROLL_SENSITIVITY;
			if(sys_scale < GalacticStrategyConstants.MIN_SCALE)
				sys_scale = GalacticStrategyConstants.MIN_SCALE;
			else if(sys_scale > GalacticStrategyConstants.MAX_SCALE)
				sys_scale = GalacticStrategyConstants.MAX_SCALE;
			
			//adjust center if part of screen goes out of bounds ////BOOKMARK
			enforceSystemBounds();
			redraw();
		}
	}

	private void enforceSystemBounds()
	{
		//adjust sys_center_x if necessary
		if(sysScreenToDataX(0)<(theinterface.getWidth()-GalacticStrategyConstants.SYS_WIDTH)/2)
			sys_center_x = (int)((theinterface.getWidth()-GalacticStrategyConstants.SYS_WIDTH)/2 + theinterface.getWidth()/(2*sys_scale));//this should be the value of center_x that makes screenToDataX equal to (SystemPanel.getWidth()-SYS_WIDTH)/2
		else if(sysScreenToDataX(theinterface.getWidth())>(theinterface.getWidth()+GalacticStrategyConstants.SYS_WIDTH)/2)
			sys_center_x = (int)((theinterface.getWidth()+GalacticStrategyConstants.SYS_WIDTH)/2 - theinterface.getWidth()/(2*sys_scale));
		//adjust sys_center_y if necessary
		if(sysScreenToDataY(0)<(theinterface.getHeight()-GalacticStrategyConstants.SYS_HEIGHT)/2)
			sys_center_y = (int)((theinterface.getHeight()-GalacticStrategyConstants.SYS_HEIGHT)/2 + theinterface.getHeight()/(2*sys_scale));
		else if(sysScreenToDataY(theinterface.getHeight())>(theinterface.getHeight()+GalacticStrategyConstants.SYS_HEIGHT)/2)
			sys_center_y = (int)((theinterface.getHeight()+GalacticStrategyConstants.SYS_HEIGHT)/2 - theinterface.getHeight()/(2*sys_scale));
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		GC.endAllThreads(); //used to end any connections, and notify other players, in addition to closing errant threads.
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentShown(ComponentEvent e){}
	
	public void componentResized(ComponentEvent e)
	{
		enforceSystemBounds();
		redraw();
	}
}