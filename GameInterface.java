import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.util.*;


public class GameInterface implements MouseListener, WindowListener, ComponentListener, MouseWheelListener, AWTEventListener
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
	HashSet<SystemLabel> label_list;
	JTextArea log;
	JPanel stat_and_order;
	JPanel theinterface;
	
	JPopupMenu select_menu;
	
	ArrayList<GSystem> known_sys; //known systems for this player
	ArrayList<Satellite<?>> known_sate;   //known satellite for this player
	static GameControl GC;
	
	GalacticMapPainter GalaxyPanel;
	double gal_scale; //the scale which the galaxy is painted at.
	Set<GSystem> selected_sys; //stores a set of currently selected systems - that is, a set of one item.  This is necessary because multiple selection is possible in GDFrame
	
	GALAXY_STATE galaxy_state;
	static enum GALAXY_STATE{NORMAL, CHOOSE_WARP_DEST, PREVIEW;}
	
	
	SystemPainter SystemPanel;
	GSystem sys,prev_sys; //doubles as the variable of the selected system in Galaxy as as the currently open system
	List<Selectable> selected_in_sys, prev_selected;
	List<Selectable> maybe_select_in_sys, maybe_deselect_in_sys; //used to assist with shift/alt support in multiple selection
	Selectable mouseover_obj; //used for mouseover highlight effect
	
	//for multi-selection in systems
	int button_down; //the button pressed in mousePressed
	boolean mouse_was_dragged; //whether we ever had any dragging between mousePressed and mouseReleased
	static enum MODIFIER {ALT, SHIFT, NONE};
	MODIFIER drag_modifier;
		double mouse_down_x;
		double mouse_down_y;
		double cur_x; //used also for mouseover effects
		double cur_y;
		
	double prev_scale,prev_x,prev_y;
	double sys_scale;
	double sys_center_x;
	double sys_center_y;
	double move_center_x_speed = 0; //left is negative, right is positive
	double move_center_y_speed = 0; //up is negative, down is positive
	MoveScreenCursors cursors;
	int recenter_delay;
	long last_time_recentered;
	
	int system_state;
		final static int SYS_NORMAL=0;
		final static int SELECT_DESTINATION=1;
		final static int ATTACK_MOVE_DESTINATION=2;
		
	static int EDGE_BOUND=20; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static int SYS_WIDTH=GalacticStrategyConstants.SYS_WIDTH; //the allowed width of a system
	static int SYS_HEIGHT=GalacticStrategyConstants.SYS_HEIGHT; //the allowed height of a system
	
	JPanel system_list;
	
	enum PANEL_DISP{SAT_PANEL, SHIP_PANEL, NONE};
	PANEL_DISP sat_or_ship_disp;
	
	PlanetMoonCommandPanel SatellitePanel;
	ShipCommandPanel ShipPanel;
	
	enum GalaxyOrSystem{Galaxy, System;}
	
	GalaxyOrSystem mode, prev_mode;
	boolean graphics_started; //used to indicate whether graphics have been started yet - that is, whether the Galaxy has been drawn yet.
	
	final static String indentation="     ";


	public GameInterface()
	{
		//create frame and layout	
		frame=new JFrame("Galactic Strategy Game");
		//frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		frame.setMinimumSize(new Dimension(800,600));
		frame.addWindowListener(this);
		frame.addComponentListener(this);
		
		//capture all mouse motion events
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
		//time.setSize(600, 200);
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
		system_list = new JPanel();
		system_list.setLayout(new BoxLayout(system_list,BoxLayout.Y_AXIS));
		pane1 = new JScrollPane(system_list);		
		pane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Systems", pane1);
		tabbedPane.setSelectedIndex(0);
		label_list = new HashSet<SystemLabel>();
		
		
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.EAST;
		c.weightx =0;
		c.weighty=0.8;
		c.gridwidth=5;
		c.gridx = 15;
		c.gridy = 1;    
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(tabbedPane,c);
		prev_scale=1.0;

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
		selected_in_sys = new ArrayList<Selectable>();
		maybe_select_in_sys = new ArrayList<Selectable>();
		maybe_deselect_in_sys = new ArrayList<Selectable>();
		mouseover_obj = null;
	
		//set up game control
		GC = new GameControl(this);
		
		system_state = SYS_NORMAL;
		galaxy_state = GALAXY_STATE.NORMAL;
		setupGraphics();
		
		//set up in-game menu for later display
		menu=new GameMenu(GC, frame);
		//set up cursors for later use
		cursors = new MoveScreenCursors();
		
		//set up select_menu for later use
		select_menu = new JPopupMenu();
		
		mouse_was_dragged=false;
		drag_modifier=MODIFIER.NONE;
		sys_scale = 1.0d;
		sys_center_x = theinterface.getWidth()/2.0;
		sys_center_y = theinterface.getHeight()/2.0;
		
		//frame.pack();
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH); //auto-maximizes the game.  Pack() would set it to whatever fit preferred size
		//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
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
		GalaxyPanel = new GalacticMapPainter(true);
		SystemPanel = new SystemPainter(false);
		
		new KeyboardHandler(SystemPanel);
		
		SatellitePanel = new PlanetMoonCommandPanel();
		ShipPanel = new ShipCommandPanel();
		
		graphics_started=false;
		sat_or_ship_disp = PANEL_DISP.NONE;
	}
	
	/**call to switch the view to the galaxy
	 * @param state the GALAXY_STATE we should load up with
	 * */
	public void drawGalaxy(GALAXY_STATE state)
	{
		System.out.println("Draw Galaxy!");
		galaxy_state = state;
		
		//this method shows the GalacticMapPainter in the main viewspace
		if(isSystemDisplayed() || !graphics_started)
		{
			theinterface.removeAll();
			theinterface.add(GalaxyPanel); //automatically adds to center
			mode=GalaxyOrSystem.Galaxy;
			prev_mode=GalaxyOrSystem.Galaxy;
			graphics_started=true;
			
			//make sure to change cursor back to normal
			theinterface.setCursor(Cursor.getDefaultCursor());
			
			if(galaxy_state == GALAXY_STATE.NORMAL)
			{	
				selected_in_sys.clear();
				displayNoPanel();
			}
		}
		gal_scale =  Math.min(((double)theinterface.getWidth())/((double)GalacticStrategyConstants.GALAXY_WIDTH), ((double)theinterface.getHeight())/((double)GalacticStrategyConstants.GALAXY_HEIGHT));
		GalaxyPanel.paintGalaxy(GC.map, selected_sys, (galaxy_state == GALAXY_STATE.CHOOSE_WARP_DEST) ? GDFrame.DRAG_RANGE : GDFrame.DRAG_NONE, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, GC.players[GC.player_id].ships_in_transit, gal_scale);
		frame.setVisible(true); //makes all components within the frame displayable.  frame.pack() does this too, but pack resizes the frame to fit all components in their preferred sizes
	}
	
	
	//before calling this function, sys should be specified
	//note that this function makes selected_in_sys null if it is used to switch from the galaxy to a system
	public void drawSystem(boolean arrow)
	{
		System.out.println("Draw System!");
		//this method shows the GalacticMapPainter in the main viewspace
		if(isGalaxyDisplayed())
		{
			theinterface.removeAll();
			theinterface.add(SystemPanel); //automatically adds to center
			mode=GalaxyOrSystem.System;
		}
/*		sys_scale = 1.0d;
		sys_center_x = theinterface.getWidth()/2;
		sys_center_y = theinterface.getHeight()/2;*/
		SystemPanel.setArrow(arrow);
		SystemPanel.paintSystem(sys, combineSelectedInSys(), sys_center_x, sys_center_y, sys_scale, true, mouse_was_dragged, mouse_down_x, mouse_down_y, cur_x, cur_y, mouseover_obj);
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
				int options=GDFrame.DRAG_NONE;
				switch(galaxy_state)
				{
					case PREVIEW:
						//TODO: should something go here?
						break;
					case NORMAL:
						options=GDFrame.DRAG_NONE;
						break;
					case CHOOSE_WARP_DEST:
						options=GDFrame.DRAG_RANGE;
						GalaxyPanel.setMaxDistShown(ShipPanel.the_ship.warpRange());
						break;
					default:
						System.out.println("redraw doesn't support galaxy_state " + galaxy_state.name());
						break;
				}
				GalaxyPanel.paintGalaxy(GC.map, selected_sys, options, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, GC.players[GC.player_id].ships_in_transit, gal_scale);
			}
			else //before getting to here, sys and selected_in_sys should be specified.
			{
				SystemPanel.paintSystem(sys, combineSelectedInSys(), sys_center_x, sys_center_y, sys_scale, true, mouse_was_dragged, mouse_down_x, mouse_down_y, cur_x, cur_y, mouseover_obj);
			}
		}
	}
	
	private List<Selectable> combineSelectedInSys()
	{
		List<Selectable> selected = new ArrayList<Selectable>();
		selected.addAll(selected_in_sys);
		selected.addAll(maybe_select_in_sys);
		selected.removeAll(maybe_deselect_in_sys);
		return selected;
	}
	
	public void displaySatellitePanel(Satellite<?> s)
	{
		SatellitePanel.setSat(s);
		
		if(sat_or_ship_disp != PANEL_DISP.SAT_PANEL)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			stat_and_order.add(SatellitePanel);
			sat_or_ship_disp = PANEL_DISP.SAT_PANEL;
		}
		
		frame.setVisible(true);
	}
	
	public void displayShipPanel(Ship s)
	{
		if(sat_or_ship_disp != PANEL_DISP.SHIP_PANEL)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			stat_and_order.add(ShipPanel);
			sat_or_ship_disp = PANEL_DISP.SHIP_PANEL;
		}
		
		ShipPanel.setShip(s);
		
		frame.setVisible(true);
	}
	
	public void displayNoPanel()
	{
		if(sat_or_ship_disp != PANEL_DISP.NONE)
		{
			stat_and_order.removeAll();
			stat_and_order.repaint();
			sat_or_ship_disp = PANEL_DISP.NONE;
			SatellitePanel.state=PlanetMoonCommandPanel.PANEL_STATE.NOT_DISPLAYED; //this is necessary to remove the bug
				//that when a facility finishes being built, and the player has nothing in the system selected,
				//and then selects a planet with no facilities, the newly built facility appears in the interface
				//as if it belongs to the selected planet, although the facility is not part of that planet's (or moon's)
				//facilities ArrayList.  setting this state ensures that displayFacility in PlanetMoonCommandPanel
				//does not try to add the new facility to the class' facilities_panel.
			
			frame.setVisible(true);
		}
	}
	
	public void switchSystemToDestinationMode()
	{
		system_state = SELECT_DESTINATION;
	}
	public void switchSystemToAttackMoveDestinationMode()
	{
		system_state = ATTACK_MOVE_DESTINATION;
	}
	

	
	public boolean isGalaxyDisplayed(){return mode == GalaxyOrSystem.Galaxy;}
	public boolean isSystemDisplayed(){return mode == GalaxyOrSystem.System;}

	//this is ONLY invoked by MOUSE MOTION EVENTS, and is in charge of deciding how fast to move the view of the system
	public void eventDispatched(AWTEvent a)
	{
		MouseEvent e=(MouseEvent)a;
		
		if(isSystemDisplayed())
		{
			//handle drag on System
			if((MouseEvent.BUTTON1_MASK & e.getModifiers()) == MouseEvent.BUTTON1_MASK && e.getSource() == theinterface && button_down == MouseEvent.BUTTON1)
			{
				if(drag_modifier == MODIFIER.NONE)
				{
					selected_in_sys.clear();
				}
				
				mouse_was_dragged=true;
				doMouseDragged(e);
			}
			else if(e.getSource() == theinterface) //just a mouse-move event
			{
				//update mouse coords for the mouse-over highlight effect
				cur_x = sysScreenToDataX(e.getX());
				cur_y = sysScreenToDataY(e.getY());
				
				if(isSystemDisplayed() && e.getX() >= theinterface.getWidth() - ImageResource.RETURN_ARROW.getWidth() && e.getY() <= ImageResource.RETURN_ARROW.getHeight())
					drawGalaxy(GALAXY_STATE.PREVIEW); //back arrow mouseovered
			}
				
			Point corner;
			if(e.getSource() instanceof JComponent)
				corner = ((JComponent)e.getSource()).getLocationOnScreen();
			else
				corner = frame.getLocationOnScreen();
			
			Point pane_pos = frame.getContentPane().getLocationOnScreen();
			int x=e.getX() - pane_pos.x + corner.x;
			int y=e.getY() - pane_pos.y + corner.y;
			
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
		else if(galaxy_state == GALAXY_STATE.PREVIEW && e.getSource()==theinterface && !(e.getX() >= theinterface.getWidth() - ImageResource.RETURN_ARROW.getWidth() && e.getY() <= ImageResource.RETURN_ARROW.getHeight()))
				drawSystem(true);
	}

	private void doMouseDragged(MouseEvent e)
	{
		if(sys.owner_id==GC.player_id||sys.owner_id==-1||!ToggleControls.fogofwar)
		{
			cur_x = sysScreenToDataX(e.getX());
			cur_y = sysScreenToDataY(e.getY());
			
			Selectable first_maybe_select = null;
			if(maybe_select_in_sys.size() != 0)
				first_maybe_select = maybe_select_in_sys.get(0);
				
			List<Selectable> mod_selection = (drag_modifier == MODIFIER.ALT) ? maybe_deselect_in_sys : maybe_select_in_sys;
			mod_selection.clear();
			selectInSystemInRange(mod_selection,	Math.min(mouse_down_x, cur_x), Math.min(mouse_down_y, cur_y),
													Math.max(mouse_down_x, cur_x), Math.max(mouse_down_y,cur_y));
		
			if(!mod_selection.contains(first_maybe_select))
			{
				if(mod_selection.size() != 0)
					first_maybe_select=mod_selection.get(0);
				else
					first_maybe_select=null;
			}
			
			List<Selectable> overall_view_ships = combineSelectedInSys();
			for(Iterator<Selectable> select_it = overall_view_ships.iterator();select_it.hasNext();)
			{
				Selectable obj = select_it.next();
				if(!(obj instanceof Ship) || ((Ship)obj).owner.getId() != GC.player_id)
				{
					select_it.remove();
				}
			}
		
			if(overall_view_ships.size() == 0) //everything is non-ship
			{
				switch(drag_modifier)
				{
					case SHIFT:
					case NONE:
						maybe_deselect_in_sys.clear();
						maybe_select_in_sys.clear();
						if(selected_in_sys.size() == 0)
							maybe_select_in_sys.add(first_maybe_select);
						break;
					case ALT:
						break;
				}
			}
			else
			{
				switch(drag_modifier)
				{
					case NONE:
					case SHIFT:
						maybe_deselect_in_sys = new ArrayList<Selectable>();
						maybe_deselect_in_sys.addAll(selected_in_sys);
						maybe_deselect_in_sys.removeAll(overall_view_ships);
						maybe_select_in_sys = overall_view_ships;
						maybe_select_in_sys.removeAll(selected_in_sys);
						break;
					case ALT:
						break;
				}
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if(isSystemDisplayed())
		{
			move_center_x_speed=0;
			move_center_y_speed=0;
		}
		else if(galaxy_state == GALAXY_STATE.PREVIEW)
		{
			//if we are previewing the galaxy, revert back to system view on mouse exit
			drawSystem(true);
		}
	}

	public void mousePressed(MouseEvent e) {
		if(isSystemDisplayed())
		{
			if(sys.owner_id==GC.player_id||sys.owner_id==-1||!ToggleControls.fogofwar)
			{
				cur_x = mouse_down_x = sysScreenToDataX(e.getX());
				cur_y = mouse_down_y = sysScreenToDataY(e.getY());
				
				button_down = e.getButton();
				
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					drag_modifier = (e.isShiftDown()) ? MODIFIER.SHIFT : ((e.isAltDown()) ? MODIFIER.ALT : MODIFIER.NONE);
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(graphics_started) {
			if(isGalaxyDisplayed())
			{
				switch(galaxy_state)
				{
					case PREVIEW:
						galaxy_state = GALAXY_STATE.NORMAL;
						selected_in_sys.clear();
						displayNoPanel();
						break;
					case NORMAL:
						selectSystemAt(((double)e.getX())/gal_scale, ((double)e.getY())/gal_scale);
						if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && sys != null)
							drawSystem(true);
						else
							redraw();
						break;
					case CHOOSE_WARP_DEST:
						GSystem the_sys = findSystemAt(((double)e.getX())/gal_scale, ((double)e.getY())/gal_scale);
						if(the_sys != null)
						{
							int x_dif = the_sys.x-sys.x;
							int y_dif = the_sys.y-sys.y;
							boolean some_warped=false; //TODO: figure out how to handle if some can warp but not others
							
							for(Selectable s : selected_in_sys)
							{
								if(s != null) //TODO: why is it null?
								{
									//TODO: draw the correct range
									int range = ShipPanel.the_ship.warpRange();
									
									if(x_dif*x_dif + y_dif*y_dif <= range*range)
									{
										//set this as the ship's warp destination
										some_warped=true;
										GC.scheduleOrder(new ShipWarpOrder(GC.players[GC.player_id], (Ship)s, GC.updater.TC.getNextTimeGrain(), the_sys));
									}
								}
								else
									some_warped = true;
							}
							
							if(some_warped)
							{
								drawSystem(true);
							}
						}
						break;
					default:
						System.out.println("galaxy state " + galaxy_state.name() +" not supported in GameInterface.mouseReleased :(");
						break;
				}
			}
			else
			{ //system is displayed
				if(sys.owner_id==GC.player_id||sys.owner_id==-1||!ToggleControls.fogofwar){
					if(mouse_was_dragged)
					{
						doMouseDragged(e);
						mouse_was_dragged=false;
						
						//combine selection permanently
						selected_in_sys.addAll(maybe_select_in_sys);
						selected_in_sys.removeAll(maybe_deselect_in_sys);
						maybe_select_in_sys.clear();
						maybe_deselect_in_sys.clear();
						
						if(selected_in_sys.size() == 1)
						{
							if(selected_in_sys.get(0) instanceof Ship)
								displayShipPanel((Ship) selected_in_sys.get(0));
							else if(selected_in_sys.get(0) instanceof Satellite<?>)
								displaySatellitePanel((Satellite<?>) selected_in_sys.get(0));
							else displayNoPanel();
						}
						else if(selected_in_sys.size() > 1)
						{
							//mass ship selection
							displayShipPanel((Ship) selected_in_sys.get(0));
						}
						else
							displayNoPanel();
					
						system_state = SYS_NORMAL;
					}
					else
					{
						if(e.getX() >= theinterface.getWidth() - ImageResource.RETURN_ARROW.getWidth() && e.getY() <= ImageResource.RETURN_ARROW.getHeight())
							drawGalaxy(GALAXY_STATE.NORMAL); //back arrow clicked
						else
						{
							if(system_state == SYS_NORMAL && e.getButton() == MouseEvent.BUTTON1)
							{
								//look for object to select
								selectInSystemAt(e.getX(), e.getY());
								redraw();
							}
							else if(system_state == SELECT_DESTINATION)
							{
								setDestination(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()),false);
								system_state = SYS_NORMAL;
							}
							else if(system_state == ATTACK_MOVE_DESTINATION)
							{
								setDestination(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()),true);
								system_state = SYS_NORMAL;
							}
							else if(selected_in_sys.size() != 0 && selected_in_sys.get(0) instanceof Ship && e.getButton() == MouseEvent.BUTTON3 && ((Ship)selected_in_sys.get(0)).owner.getId() == GC.player_id)
							{
								setDestination(sysScreenToDataX(e.getX()), sysScreenToDataY(e.getY()),false);
							}
						}
					}
				}
				else
					mouse_was_dragged=false;
			}
		}
	}
	
	private void selectSystemAt(double x, double y) //arguments specified in Galaxy coordinates, not screen coordinates
	{
		sys=findSystemAt(x,y);
		selected_sys.clear(); //deselect all
		if(sys != null)
			selected_sys.add(sys); //if found system, add it to the selection
	}
	
	/**The arguments are specified in Galaxy coordinates, not screen coordinates.
	 * 
	 * @param x x-coordinate in the Galaxy to look for a system at
	 * @param y y-coordinate in the Galaxy to look for a system at
	 * @return the system that is closest, if it is within SELECTION_TOLERANCE.  If no GSystem is within the tolerance, it returns null.
	 * 
	 * @see GalacticStrategyConstants.SELECTION_TOLERANCE
	 * */
	private GSystem findSystemAt(double x, double y)
	{
		for(GSystem the_sys : GC.map.systems) //TODO: this should search through GC.players[GC.player_id].known_systems
		{
			double dif_x = the_sys.x - x;
			double dif_y = the_sys.y - y;
			
			if(dif_x*dif_x + dif_y*dif_y <= GalacticStrategyConstants.SELECTION_TOLERANCE*GalacticStrategyConstants.SELECTION_TOLERANCE)
				return the_sys;
		}
		return null; //no system found
	}
	
	private void selectInSystemAt(int mouse_x, int mouse_y)
	{
		//TODO: remove unused code...
		
		/*final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/sys_scale; //tolerance
		double x = sysScreenToDataX(mouse_x);
		double y = sysScreenToDataY(mouse_y);
		
		ArrayList<Selectable> select_items = new ArrayList<Selectable>();
		
		selectInSystemInRange(select_items, x-OBJ_TOL, y-OBJ_TOL, x+OBJ_TOL, y+OBJ_TOL);*/
		
		/*if(select_items.size() > 1)
			buildSelectContextMenu(select_items, mouse_x, mouse_y);
		else if(select_items.size()==1)
			selectObjInSystem(select_items.get(0));*/
		
		if(mouseover_obj != null)
			selectObjInSystem(mouseover_obj);
		else //if nothing found
		{
			selected_in_sys.clear();
			displayNoPanel();
		}
	}
	
	private void selectInSystemInRange(List<Selectable> select_items, double x1, double y1, double x2, double y2)
	{
		if(sys.stars != null)
		{
			for(Star st : sys.stars)
			{
				//search for star...
				if(st.x-st.size/2 <= x2 && x1 <= st.x+st.size/2 && st.y-st.size/2 <= y2 && y1 <= st.y+st.size/2)
				{
					select_items.add(st);
				}
			}
		}
		
		//search orbiting planets/objects
		if(sys.orbiting != null)
		{
			for(Satellite<?> orbiting : sys.orbiting)
			{
				//search for satellites...
				if(orbiting.absoluteCurX()-orbiting.size/2 <= x2 && x1 <= orbiting.absoluteCurX()+orbiting.size/2 && orbiting.absoluteCurY()-orbiting.size/2 <= y2 && y1 <= orbiting.absoluteCurY() + orbiting.size/2)
				{
					select_items.add(orbiting);
				}
				
				if(orbiting instanceof Planet && ((Planet)(orbiting)).orbiting != null)
				{
					Planet cur_planet=(Planet)orbiting;
					for(Satellite<?> sat : cur_planet.orbiting)
					{
						if(sat.absoluteCurX()-sat.size/2 <= x2 && x1 <= sat.absoluteCurX()+sat.size/2 && sat.absoluteCurY()-sat.size/2 <= y2 && y1 <= sat.absoluteCurY()+sat.size/2)
						{
							select_items.add(sat);
						}
					}
				}
			}
		}
		
		for(int i=0; i<sys.fleets.length; i++)
		{
			for(Ship.ShipId j : sys.fleets[i].ships.keySet())
			{
				Ship s = sys.fleets[i].ships.get(j);
				
				if(s.pos_x-s.type.dim*s.type.img.scale/2 <= x2 && x1 <= s.pos_x+s.type.dim*s.type.img.scale/2 && s.pos_y-s.type.dim*s.type.img.scale/2 <= y2 && y1 <= s.pos_y+s.type.dim*s.type.img.scale/2)
				{
					select_items.add(s);
				}
			}
		}
	}
	
	public void selectObjInSystem(Selectable s)
	{
		selected_in_sys.clear();
		selected_in_sys.add(s);
		switch(s.getSelectType())
		{
			case Selectable.STAR:
				displayNoPanel();
				break;
			case Selectable.SATELLITE:
				displaySatellitePanel((Satellite<?>)s);
				break;
			case Selectable.SHIP:
				displayShipPanel((Ship)s);
				break;
			default:
				System.out.println("selection unsupported by selectObjInSystem!!");
				break;
		}
	}
	
	@Deprecated
	public void buildSelectContextMenu(ArrayList<Selectable> select_which, int x, int y)
	{
		select_menu.removeAll();
		for(Selectable s : select_which)
		{
			SelectableMenuItem menu_item = new SelectableMenuItem(s);
			menu_item.addActionListener(menu_item);
			select_menu.add(menu_item);
		}
		
		select_menu.show(theinterface,x,y);
	}
	
	private void setDestination(double x, double y, boolean AttackMove)
	{
		Destination<?> dest = new DestinationPoint(x,y);
		
		//1st search out satellites
		
		final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/sys_scale; //tolerance
		
		//search orbiting planets/objects
		if(sys.orbiting != null)
		{
			for(Satellite<?> sat : sys.orbiting)
			{
				//search for satellites...
				if(sat.absoluteCurX()-sat.size/2 -OBJ_TOL <= x && x <= sat.absoluteCurX()+sat.size/2 + OBJ_TOL && sat.absoluteCurY()-sat.size/2-OBJ_TOL <= y && y <= sat.absoluteCurY() + sat.size/2+OBJ_TOL)
				{
					dest = sat;
					break;
				}
				
				if(sat instanceof Planet && ((Planet)(sat)).orbiting != null)
				{
					Planet cur_planet=(Planet)sat;
					for(Satellite<?> sat2 : cur_planet.orbiting)
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
			for(Ship.ShipId j : sys.fleets[i].ships.keySet())
			{
				Ship s = sys.fleets[i].ships.get(j);
				
				if(s.pos_x-OBJ_TOL-s.type.dim*s.type.img.scale/2*sys_scale <= x && x <= s.pos_x+OBJ_TOL+s.type.dim*s.type.img.scale/2 && s.pos_y-OBJ_TOL-s.type.dim*s.type.img.scale/2 <= y && y <= s.pos_y+OBJ_TOL+s.type.dim*s.type.img.scale/2)
				{
					dest = s;
					break;
				}
			}
		}
		
		long time = GC.updater.getTime();
		for(Selectable the_ship : selected_in_sys)
		{
			if(the_ship == null)
				System.out.println("null object in selected");
			
			if(the_ship != null)
			{
				if(AttackMove)
				{
					if(dest instanceof Ship && dest != ShipPanel.the_ship)
					{
						GC.scheduleOrder(new ShipAttackOrder(GC.players[GC.player_id], (Ship)the_ship, TimeControl.getTimeGrainAfter(time), time, (Targetable<?>)dest));
					}	
					else{
						GC.scheduleOrder(new ShipAttackMoveOrder(GC.players[GC.player_id], (Ship)the_ship, TimeControl.getTimeGrainAfter(time), dest));
					}
					//ShipPanel.updateDestDisplay(dest); //not a bad idea, but its a no-op, since we just only scheduled the order
				}
				else{
					GC.scheduleOrder(new ShipMoveOrder(GC.players[GC.player_id], (Ship)the_ship, TimeControl.getTimeGrainAfter(time), dest));
					
					//TODO: work on correct updating
					ShipPanel.updateDestDisplay(dest);
					
					//TODO: fix if more than one team or more than two players to only target enemy
					if(dest instanceof Ship && dest != ShipPanel.the_ship&&((Ship)dest).owner != GC.players[GC.player_id])
					{
						GC.scheduleOrder(new ShipAttackOrder(GC.players[GC.player_id], (Ship)the_ship, TimeControl.getTimeGrainAfter(time), time, (Targetable<?>)dest));
					}
				}
			}
		}
	}
	
	public void update(long t)      					//TODO: use only known systems
	{		
		if (!labels_made)
		{
			System.out.println("made");
			known_sys=GC.map.systems;//GC.players[GC.player_id].known_systems;
				
			labels_made=true;									
			system_list.removeAll();		
			for (GSystem system :known_sys)
			{
				SystemLabel label=new SystemLabel(system,this);
				system_list.add(label);
				label_list.add(label);
			}
		}
		else
		{
			for(SystemLabel l : label_list)
			{
				l.update(t);
			}
		}
		
		if(isSystemDisplayed())
		{
			//mouseover effect
			final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/sys_scale; //tolerance
			
			ArrayList<Selectable> select_items = new ArrayList<Selectable>();
			
			selectInSystemInRange(select_items, cur_x-OBJ_TOL, cur_y-OBJ_TOL, cur_x+OBJ_TOL, cur_y+OBJ_TOL);
			mouseover_obj = (select_items.size() != 0) ? select_items.get(0) : null;
		}
		
		moveCenter();
		frame.validate();
	}
	
	private void moveCenter()
	{
		long cur_time = GC.updater.getTime();
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
		
		//enforce bounds
		if(sysScreenToDataX(0)<(theinterface.getWidth()-SYS_WIDTH)/2.0 || sysScreenToDataX(theinterface.getWidth())>(theinterface.getWidth()+SYS_WIDTH)/2.0)
			sys_center_x = old_center_x;
		
		if(sysScreenToDataY(0)<(theinterface.getHeight()-SYS_HEIGHT)/2.0 || sysScreenToDataY(theinterface.getHeight())>(theinterface.getHeight()+SYS_HEIGHT)/2.0)
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
			
			//adjust center if part of screen goes out of bounds //TODO: glitchy bounds-control needs fixing
			enforceSystemBounds();
			redraw();
		}
	}

	private void enforceSystemBounds()
	{
		//adjust sys_center_x if necessary
		if(sysScreenToDataX(0)<(theinterface.getWidth()-GalacticStrategyConstants.SYS_WIDTH)/2.0)
			sys_center_x = (int)((theinterface.getWidth()-GalacticStrategyConstants.SYS_WIDTH)/2.0 + theinterface.getWidth()/(2*sys_scale));//this should be the value of center_x that makes screenToDataX equal to (SystemPanel.getWidth()-SYS_WIDTH)/2
		else if(sysScreenToDataX(theinterface.getWidth())>(theinterface.getWidth()+GalacticStrategyConstants.SYS_WIDTH)/2.0)
			sys_center_x = (int)((theinterface.getWidth()+GalacticStrategyConstants.SYS_WIDTH)/2.0 - theinterface.getWidth()/(2*sys_scale));
		//adjust sys_center_y if necessary
		if(sysScreenToDataY(0)<(theinterface.getHeight()-GalacticStrategyConstants.SYS_HEIGHT)/2.0)
			sys_center_y = (int)((theinterface.getHeight()-GalacticStrategyConstants.SYS_HEIGHT)/2.0 + theinterface.getHeight()/(2*sys_scale));
		else if(sysScreenToDataY(theinterface.getHeight())>(theinterface.getHeight()+GalacticStrategyConstants.SYS_HEIGHT)/2.0)
			sys_center_y = (int)((theinterface.getHeight()+GalacticStrategyConstants.SYS_HEIGHT)/2.0 - theinterface.getHeight()/(2*sys_scale));
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