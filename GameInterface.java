import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.util.*;


public class GameInterface implements ActionListener, MouseMotionListener, MouseListener, WindowListener, ComponentListener
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
	GameControl GC;
	
	GalacticMapPainter GalaxyPanel;
	double gal_scale; //the scale which the galaxy is painted at.
	HashSet<GSystem> selected_sys; //stores a set of currently selected systems - that is, a set of one item.  This is necessary because multiple selection is possible in GDFrame
	
	
	SystemPainter SystemPanel;
	GSystem sys,prev_sys; //doubles as the variable of the selected system in Galaxy as as the currently open system
	Selectable selected_in_sys, prev_selected;
	double sys_scale;
	double sys_center_x;
	double sys_center_y;
	int system_state;
		final static int NORMAL=0;
		final static int SELECT_DESTINATION=1;
	
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
		
		frame.setVisible(true);	
		GC.startupDialog();
	}	
	
	//for the tabbed pane
    /*protected JPanel makeTextPanel (String text) 
    {
        JPanel apanel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        apanel.setLayout(new GridLayout(1, 1));
        apanel.add(filler);
        return apanel;
    }*/
    
    
	

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable(){public void run(){new GameInterface();}});
	}
	
	private void setupGraphics()
	{
		//sets up GalacticMapPainter, SystemPainter
		GalaxyPanel = new GalacticMapPainter(GC);
		SystemPanel = new SystemPainter(false);
		
		SatellitePanel = new PlanetMoonCommandPanel(GC);
		ShipPanel = new ShipCommandPanel(this);
		
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
		sys_scale = 1.0d;
		sys_center_x = theinterface.getWidth()/2;
		sys_center_y = theinterface.getHeight()/2;
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

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
		if(sys.orbiting_objects instanceof ArrayList)
		{
			for(Satellite orbiting : sys.orbiting_objects)
			{
				//search for satellites...
				if(orbiting.absoluteCurX()-orbiting.size/2 <= x && x <= orbiting.absoluteCurX()+orbiting.size/2 && orbiting.absoluteCurY()-orbiting.size/2 <= y && y <= orbiting.absoluteCurY() + orbiting.size/2)
				{
					selected_in_sys=orbiting;
					displaySatellitePanel(orbiting);
					return;
				}
				
				if(orbiting instanceof Planet && ((Planet)(orbiting)).satellites instanceof Set)
				{
					Planet cur_planet=(Planet)orbiting;
					for(Satellite sat : cur_planet.satellites)
					{
						if(sat.absoluteCurX()-OBJ_TOL <= x && x <= sat.absoluteCurX()+OBJ_TOL && sat.absoluteCurY()-OBJ_TOL <= y && y <= sat.absoluteCurY()+OBJ_TOL)
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
				
				if(s.pos_x-2*OBJ_TOL <= x && x <= s.pos_x+2*OBJ_TOL && s.pos_y-2*OBJ_TOL <= y && y <= s.pos_y+2*OBJ_TOL)
				{
					System.out.println("ship selected!");
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
		if(sys.orbiting_objects instanceof ArrayList)
		{
			for(Satellite orbiting : sys.orbiting_objects)
			{
				//search for satellites...
				if(orbiting.absoluteCurX()-orbiting.size/2 <= x && x <= orbiting.absoluteCurX()+orbiting.size/2 && orbiting.absoluteCurY()-orbiting.size/2 <= y && y <= orbiting.absoluteCurY() + orbiting.size/2)
				{
					dest = orbiting;
					break;
				}
				
				if(orbiting instanceof Planet && ((Planet)(orbiting)).satellites instanceof Set)
				{
					Planet cur_planet=(Planet)orbiting;
					for(Satellite sat : cur_planet.satellites)
					{
						if(sat.absoluteCurX()-OBJ_TOL <= x && x <= sat.absoluteCurX()+OBJ_TOL && sat.absoluteCurY()-OBJ_TOL <= y && y <= sat.absoluteCurY()+OBJ_TOL)
						{
							dest=sat;
							break;
						}
					}
				}
			}
		}
		
		ShipPanel.the_ship.destination = dest;
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
		frame.setVisible(true);
	}
	
	private double sysScreenToDataX(int x)
	{
		return (x-theinterface.getWidth()/2)/sys_scale+sys_center_x;
	}
	
	private double sysScreenToDataY(int y)
	{
		return (y-theinterface.getHeight()/2)/sys_scale+sys_center_y;
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
		redraw();
	}
}