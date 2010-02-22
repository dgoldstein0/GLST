import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class SystemViewer extends JDialog implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, WindowListener
{
	static int DEFAULT_STAR_SIZE=GalacticStrategyConstants.DEFAULT_STAR_SIZE;
	static int DEFAULT_STAR_ZONE_SIZE=GalacticStrategyConstants.DEFAULT_STAR_ZONE_SIZE;
	static int DEFAULT_PLANET_SIZE = GalacticStrategyConstants.DEFAULT_PLANET_SIZE;
	static int DEFAULT_MOON_SIZE = GalacticStrategyConstants.DEFAULT_MOON_SIZE;
	static double DEFAULT_PLANET_MASS = GalacticStrategyConstants.DEFAULT_PLANET_MASS;
	static double DEFAULT_STAR_MASS = GalacticStrategyConstants.DEFAULT_STAR_MASS;
	static double DEFAULT_MOON_MASS= GalacticStrategyConstants.DEFAULT_MOON_MASS;
	
	final static int ADD_NOTHING=0;
	final static int ADD_STAR=1;
	final static int ADD_PLANET=2;
	final static int ADD_ASTEROID=3;
	final static int ADD_MOON=4;
	final static int ADD_FOCUS=5;
	final static int RECENTER=6;
	
	static int EDGE_BOUND=GalacticStrategyConstants.EDGE_BOUND; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static int SYS_WIDTH=GalacticStrategyConstants.SYS_WIDTH; //the allowed width of a system
	static int SYS_HEIGHT=GalacticStrategyConstants.SYS_HEIGHT; //the allowed height of a system
	
	//sets parameters for scrolling the system view around
	int move_center_x_speed = 0; //left is negqtive, right is positive
	int move_center_y_speed = 0; //up is negative, down is positive
	
	int wait_to_add = ADD_NOTHING;
	boolean drag_start;
	
	GSystem system;
	SystemPainter painter;
	Selectable selected_obj;
	JFrame frame;
	UndoRedoStack undostack;
	
	//toolbar elements here
	JButton t_toggle;
	JButton t_add;
	JButton t_delete;
	JButton t_edit;
	JButton t_recenter;
	JButton t_time;
	JButton t_reset_time;
	
	//elements used for the popup menu
	JPopupMenu context_menu;
	JMenuItem c_add;
	JMenuItem c_delete;
	JMenuItem c_edit;
	JMenuItem c_recenter;
	JMenuItem c_time;
	JMenuItem c_reset_time;
	
	//these integers save the position the mouse click that opens the context menu
	int c_x;
	int c_y;
	
	//objects used to track time flow for time simulation
	TimeControl TC;
	UpdateTask task;
	java.util.Timer camera_timer;
	centerMover recenter_task;
	
	//sets up the viewpoint for the system
	int center_x;
	int center_y;
	double scale=GalacticStrategyConstants.DEFAULT_SCALE;
	
	//sets up custom cursors
	Cursor left_arrow;
	Cursor right_arrow;
	Cursor up_arrow;
	Cursor down_arrow;
	Cursor up_right_arrow;
	Cursor up_left_arrow;
	Cursor down_right_arrow;
	Cursor down_left_arrow;
	
	public SystemViewer(JFrame frame, GSystem sys)
	{
		super(frame, "System Viewer", false);
		setResizable(false);
		
		this.frame=frame;
		system=sys;
		
		addWindowListener(this);
		
		painter = new SystemPainter(true);
		painter.addMouseListener(this);
		painter.addMouseMotionListener(this);
		painter.addMouseWheelListener(this);
		painter.addKeyListener(this);
		add(painter, BorderLayout.CENTER);
		
		//Toolbar code start
		JToolBar toolbar=new JToolBar("System Toolbar");
		
		t_toggle = new JButton("Toggle View");
		t_toggle.setMnemonic(KeyEvent.VK_T);
		t_toggle.addActionListener(this);
		toolbar.add(t_toggle);
		
		t_add = new JButton("Add Object");
		t_add.setMnemonic(KeyEvent.VK_A);
		t_add.addActionListener(this);
		toolbar.add(t_add);
		
		t_delete = new JButton("Delete");
		t_delete.setMnemonic(KeyEvent.VK_D);
		t_delete.addActionListener(this);
		toolbar.add(t_delete);
		
		painter.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"delete");
		painter.getActionMap().put("delete", delete_action);
		
		t_edit = new JButton("Edit");
		t_edit.setMnemonic(KeyEvent.VK_E);
		t_edit.addActionListener(this);
		toolbar.add(t_edit);
		
		t_recenter = new JButton("Recenter");
		t_recenter.setMnemonic(KeyEvent.VK_C);
		t_recenter.addActionListener(this);
		toolbar.add(t_recenter);
		
		t_time = new JButton("Start Time");
		t_time.setMnemonic(KeyEvent.VK_S);
		t_time.addActionListener(this);
		toolbar.add(t_time);
		
		t_reset_time = new JButton("Reset Time");
		t_reset_time.setMnemonic(KeyEvent.VK_R);
		t_reset_time.addActionListener(this);
		toolbar.add(t_reset_time);
		
		add(toolbar, BorderLayout.NORTH);
		
		//start context menu code////////////////////
		context_menu = new JPopupMenu();
		
		c_add = new JMenuItem("Add");
		c_add.addActionListener(this);
		context_menu.add(c_add);
		
		c_edit = new JMenuItem("Edit");
		c_edit.addActionListener(this);
		context_menu.add(c_edit);
		
		c_delete=new JMenuItem("Delete");
		c_delete.addActionListener(this);
		context_menu.add(c_delete);
		
		c_recenter = new JMenuItem("Recenter");
		c_recenter.addActionListener(this);
		context_menu.add(c_recenter);
		
		c_time=new JMenuItem("Start Time");
		c_time.addActionListener(this);
		context_menu.add(c_time);
		
		c_reset_time=new JMenuItem("Reset Time");
		c_reset_time.addActionListener(this);
		context_menu.add(c_reset_time);
		//end context menu code///////////////////
		
		Object[] o = {system, selected_obj};
		undostack = new UndoRedoStack(o);
		
		pack();
		setSize(800,650);
		

		ObjectSelected(false);
		//set up timing device for camera motion
		camera_timer = new java.util.Timer(true);
		recenter_task = new centerMover();
		
		//set up custom cursor
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/right_arrow.png"), new Point(30,15), "right arrow");
		left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/left_arrow.png"), new Point(0,15), "left arrow");
		up_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_arrow.png"), new Point(15,0), "up arrow");
		down_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_arrow.png"), new Point(15,30), "down arrow");
		
		up_right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_right_arrow.png"), new Point(30,0), "up right arrow");
		up_left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_left_arrow.png"), new Point(0,0), "up left arrow");
		down_right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_right_arrow.png"), new Point(30,30), "down right arrow");
		down_left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_left_arrow.png"), new Point(0,30), "down left arrow");
		
		//it is necessary to invoke this later because before the systemviewer is setvisible it has no height and width, which drawSystem() uses to determine the height/width of GSystem, which is then referenced in absoluteCurX/CurY/InitX/InitY to determine where the center of the system is for coordinate purposes.
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			center_x=painter.getWidth()/2;
			center_y=painter.getHeight()/2;
			drawSystem();}});
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==t_toggle)
			painter.paintSystem(system, selected_obj, !painter.design_view, center_x, center_y, scale);
		else if(e.getSource()==t_add)
		{
			String obj_to_add;
			if(!(selected_obj instanceof Planet))
			{
				String[] add_options={"Planet", "Star", "Asteroid"};
				obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
			}
			else
			{
				String[] add_options={"Planet", "Star", "Moon", "Asteroid"};
				obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
			}
			
			if(obj_to_add=="Star")
				wait_to_add=ADD_STAR;
			else if(obj_to_add=="Planet")
				wait_to_add=ADD_PLANET;
			else if(obj_to_add=="Asteroid")
				wait_to_add=ADD_ASTEROID;
			else if(obj_to_add=="Moon")
				wait_to_add=ADD_MOON;
			t_recenter.setEnabled(false);
			c_recenter.setEnabled(false);
		}
		else if(e.getSource() == c_add) //here, the placement of the system has already been determined by a mouse click, and stored in c_x and c_y
		{
			String obj_to_add;

			//Moons and stars may only be added under certain conditions.  For moons, that condition is that the planet they will orbit is already selected.
			//For stars, that condition is that the location is "star suitable."  The if statements take care of these conditions
			if(!(selected_obj instanceof Planet))
			{
				if(locationStarSuitable(screenToDataX(c_x),screenToDataY(c_y)))
				{
					String[] add_options={"Planet", "Star", "Asteroid"};
					obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
				}
				else
				{
					String[] add_options={"Planet", "Asteroid"};
					obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
				}
			}
			else
			{
				if(locationStarSuitable(screenToDataX(c_x),screenToDataY(c_y)))
				{
					String[] add_options={"Planet", "Star", "Moon", "Asteroid"};
					obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
				}
				else
				{
					String[] add_options={"Planet", "Moon", "Asteroid"};
					obj_to_add=(String)JOptionPane.showInputDialog(this, "Select the type of object to add", "Add Object", JOptionPane.QUESTION_MESSAGE, null, add_options, add_options[0]);
				}
			}
			
			if(obj_to_add=="Star")
			{
				if(!addStar(screenToDataX(c_x), screenToDataY(c_y)))
					JOptionPane.showMessageDialog(this, "You can't place a star there!", "Invalid star location", JOptionPane.ERROR_MESSAGE);
			}
			else if(obj_to_add=="Planet")
				addPlanet(screenToDataX(c_x),screenToDataY(c_y));
			//else if(obj_to_add=="Asteroid")
			//	;//no routine yet
			else if(obj_to_add=="Moon")
				addMoon(screenToDataX(c_x),screenToDataY(c_y));
			if(wait_to_add != ADD_FOCUS)
				wait_to_add=ADD_NOTHING;
			drawSystem();
		}
		else if(e.getSource()==t_delete || e.getSource() == c_delete)
			deleteSelected();
		else if(e.getSource()==t_edit || e.getSource()==c_edit)
			editSelected();
		else if(e.getSource()==t_recenter)
		{
			wait_to_add=RECENTER;
		}
		else if(e.getSource() == c_recenter)
		{
			setCenter(screenToDataX(c_x),screenToDataY(c_y));
		}
		else if(e.getSource()==t_time || e.getSource()==c_time)
		{
			if(TC instanceof TimeControl)
				TC.resetTime(0);
			else
				TC = new TimeControl(0);

			TC.startConstIntervalTask(new UpdateTask(),20);
		}
		else if(e.getSource()==t_reset_time || e.getSource() == c_reset_time)
		{
			//clear timer
			if(TC instanceof TimeControl)
			{
				TC.stopTask();
				TimeUpdater(0);
			}
		}
	}
	
	private class UpdateTask extends TimerTask
	{
		public void run()
		{
			TimeUpdater(TC.getTime());
		}
	}
	
	private void TimeUpdater(long time)
	{
		if(system.orbiting_objects instanceof HashSet)
		{
			for(Satellite sat : system.orbiting_objects)
			{
				//update orbit of sat
				sat.orbit.move(time);
				if(sat instanceof Planet)
				{
					if(((Planet)sat).satellites instanceof HashSet)
					{
						for(Satellite sat2 : ((Planet)sat).satellites)
						{
							//update orbit of sat2
							sat2.orbit.move(time);
						}
					}
				}
			}
		}
		
		drawSystem();
	}
	
	private class centerMover extends TimerTask
	{
		public void run()
		{
			moveCenter();
		}
	}
	
	private void moveCenter()
	{
		center_x += move_center_x_speed;
		center_y += move_center_y_speed;
		
		if(screenToDataX(0)<(painter.getWidth()-SYS_WIDTH)/2 || screenToDataX(painter.getWidth())>(painter.getWidth()+SYS_WIDTH)/2)
			center_x -= move_center_x_speed;
		
		if(screenToDataY(0)<(painter.getHeight()-SYS_HEIGHT)/2 || screenToDataY(painter.getHeight())>(painter.getHeight()+SYS_HEIGHT)/2)
			center_y -= move_center_y_speed;
		drawSystem();
	}
	
	private void ObjectSelected(boolean select)
	{
		t_delete.setEnabled(select);
		t_edit.setEnabled(select);
		c_delete.setEnabled(select);
		c_edit.setEnabled(select);
	}
	
	private void locateObj(int x, int y) throws NoObjectLocatedException
	{
		//search for stars, then orbiting objects and their satellites
		final double OBJ_TOL = GalacticStrategyConstants.SELECTION_TOLERANCE/scale; //tolerance
		
		if(system.stars instanceof HashSet)
		{
			for(Star st : system.stars)
			{
				//search for star...
				if(st.x-st.size/2 <= x && x <= st.x+st.size/2 && st.y-st.size/2 <= y && y <= st.y+st.size/2)
				{
					selected_obj = st;
					return;
				}
			}
		}
		
		//search orbiting planets/objects
		if(system.orbiting_objects instanceof Set)
		{
			for(Satellite orbiting : system.orbiting_objects)
			{
				//search for satellites...
				if(orbiting.absoluteCurX()-orbiting.size/2 <= x && x <= orbiting.absoluteCurX()+orbiting.size/2 && orbiting.absoluteCurY()-orbiting.size/2 <= y && y <= orbiting.absoluteCurY() + orbiting.size/2)
				{
					selected_obj=orbiting;
					return;
				}
				
				if(orbiting instanceof Planet && ((Planet)(orbiting)).satellites instanceof Set)
				{
					Planet cur_planet=(Planet)orbiting;
					for(Satellite sat : cur_planet.satellites)
					{
						if(sat.absoluteCurX()-OBJ_TOL <= x && x <= sat.absoluteCurX()+OBJ_TOL && sat.absoluteCurY()-OBJ_TOL <= y && y <= sat.absoluteCurY()+OBJ_TOL)
						{
							selected_obj=sat;
							return;
						}
					}
				}
			}
			
			if(selected_obj instanceof Satellite) {//check to see if focus2 of the selected planet was just clicked on
				if(((Satellite)selected_obj).orbit.focus2.getX()+((Satellite)selected_obj).orbit.boss.absoluteCurX() - OBJ_TOL <= x &&
					x <= ((Satellite)selected_obj).orbit.focus2.getX()+((Satellite)selected_obj).orbit.boss.absoluteCurX()+OBJ_TOL &&
					((Satellite)selected_obj).orbit.focus2.getY()+((Satellite)selected_obj).orbit.boss.absoluteCurY()-OBJ_TOL <= y &&
					y <= ((Satellite)selected_obj).orbit.focus2.getY()+((Satellite)selected_obj).orbit.boss.absoluteCurY()+OBJ_TOL)
				{
					selected_obj = ((Satellite)selected_obj).orbit.focus2;//select the focus!
					return;
				}
			}
			else if(selected_obj instanceof Focus) //if a focus is selected
			{
				if(((Focus)selected_obj).getX()+((Focus)selected_obj).owner.boss.absoluteCurX() - OBJ_TOL <= x &&
					x <= ((Focus)selected_obj).getX()+((Focus)selected_obj).owner.boss.absoluteCurX()+OBJ_TOL &&
					((Focus)selected_obj).getY()+((Focus)selected_obj).owner.boss.absoluteCurY()-OBJ_TOL <= y &&
					y <= ((Focus)selected_obj).getY()+((Focus)selected_obj).owner.boss.absoluteCurY()+OBJ_TOL)
				{
					return; //the Focus is already selected!
				}
			}
		}
		
		throw new NoObjectLocatedException(x, y);
	}
	
	private class NoObjectLocatedException extends Exception
	{
		int x;
		int y;
		
		private NoObjectLocatedException(int x, int y)
		{
			super("NoObjectLocatedException from point ("+Integer.toString(x)+","+Integer.toString(y)+").");
			
			this.x=x;
			this.y=y;
		}
	}
	
	//keylistener code
	public void keyPressed(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e)
	{
		if(undostack.undoPossible() && UndoRedoStack.isCtrlZ(e))
		{
			Object[] o = undostack.undoLoad();
			system = (GSystem)o[0];
			selected_obj = (StellarObject)o[1];
			recalculateOrbits(); //necessary since the orbit data is not all saved, but calculated from initial placement
			drawSystem();
		}
		else if(undostack.redoPossible() && UndoRedoStack.isCtrlY(e))
		{
			//System.out.println("redo");
			Object[] o = undostack.redoLoad();
			system = (GSystem)o[0];
			selected_obj = (StellarObject)o[1];
			recalculateOrbits(); //necessary since the orbit data is not all saved, but calculated from initial placement
			drawSystem();
		}
	}
	
	//mouselistener code
	public void mousePressed(MouseEvent e)
	{
		if(wait_to_add==ADD_NOTHING)
		{
			try
			{
				locateObj(screenToDataX(e.getX()),screenToDataY(e.getY()));
				ObjectSelected(true);
				drawSystem();
				
				drag_start=true;
			}
			catch(NoObjectLocatedException x){//drag_start=false; //this happens automatically, in mouseReleased.
			}
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
		if(drag_start)
		{
			setUndoPoint();
			drag_start=false;
		}
		
		if(!e.isPopupTrigger()) //(usually) left click
		{
			if(wait_to_add==ADD_NOTHING)
			{
				try
				{
					locateObj(screenToDataX(e.getX()),screenToDataY(e.getY()));
					ObjectSelected(true);
					drawSystem();
				}
				catch(NoObjectLocatedException x)
				{
					ObjectSelected(false);
					selected_obj=null;
					drawSystem();
				}
				
				if(selected_obj instanceof StellarObject && e.getClickCount()==2)
					editSelected();
			}
			else
			{
				switch(wait_to_add)
				{
					case ADD_STAR:
						addStar(screenToDataX(e.getX()), screenToDataY(e.getY()));
						break;
					case ADD_PLANET:
						addPlanet(screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
					case ADD_ASTEROID:
						break;
					case ADD_MOON:
						addMoon(screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
					case ADD_FOCUS:
						wait_to_add=ADD_NOTHING;
						setUndoPoint();
						break;
					case RECENTER:
						setCenter(screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
				}
				if(wait_to_add != ADD_FOCUS)
				{
					wait_to_add=ADD_NOTHING;
					t_recenter.setEnabled(true);
					c_recenter.setEnabled(true);
				}
				ObjectSelected(true);
			}
		}
		else //popup trigger = (usually) right click
		{
			c_x=e.getX();
			c_y=e.getY();
			context_menu.show(e.getComponent(), c_x, c_y);
		}
	}
	
	//implements zooming
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		scale -= ((double)e.getWheelRotation())*GalacticStrategyConstants.SCROLL_SENSITIVITY;
		if(scale < GalacticStrategyConstants.MIN_SCALE)
			scale = GalacticStrategyConstants.MIN_SCALE;
		else if(scale > GalacticStrategyConstants.MAX_SCALE)
			scale = GalacticStrategyConstants.MAX_SCALE;
		
		//adjust center if part of screen goes out of bounds ////BOOKMARK
		if(screenToDataX(0)<(painter.getWidth()-SYS_WIDTH)/2)
			center_x = (int)((painter.getWidth()-SYS_WIDTH)/2 + painter.getWidth()/(2*scale));//this should be the value of center_x that makes screenToDataX equal to (painter.getWidth()-SYS_WIDTH)/2
		else if(screenToDataX(painter.getWidth())>(painter.getWidth()+SYS_WIDTH)/2)
			center_x = (int)((painter.getWidth()+SYS_WIDTH)/2 - painter.getWidth()/(2*scale));
		
		if(screenToDataY(0)<(painter.getHeight()-SYS_HEIGHT)/2)
			center_y = (int)((painter.getHeight()-SYS_HEIGHT)/2 + painter.getHeight()/(2*scale));
		else if(screenToDataY(painter.getHeight())>(painter.getHeight()+SYS_HEIGHT)/2)
			center_y = (int)((painter.getHeight()+SYS_HEIGHT)/2 - painter.getHeight()/(2*scale));
		
		drawSystem();
	}
	
	//center_x = (painter.getWidth()+SYS_WIDTH)/2 - ((x-painter.getWidth()/2)/scale)
		
	private void setCenter(int x, int y)
	{
		center_x=x;
		center_y=y;
		drawSystem();
	}

	private void setUndoPoint()
	{
		Object[] o = {system, selected_obj};
		undostack.objectsChanged(o);
	}
	
	private boolean addStar(int x, int y)
	{
		boolean success = locationStarSuitable(x, y);
		if(success)
		{
			if (!(system.stars instanceof HashSet))
				system.stars=new HashSet<Star>();
			Star new_star = new Star("", DEFAULT_STAR_SIZE, DEFAULT_STAR_MASS, GalacticStrategyConstants.COLOR_NULL, x, y, system);
			system.stars.add(new_star);
			
			selected_obj=new_star;
			recalculateOrbits();
		}
		
		drawSystem();
		setUndoPoint();
		return success;
	}
	
	private void recalculateOrbits()
	{
		if(system.orbiting_objects instanceof HashSet)
		{
			for(Satellite sat: system.orbiting_objects)
			{
				sat.orbit.calculateOrbit();
			}
		}
	}
	
	private int screenToDataX(int x)
	{
		return (int)((x-painter.getWidth()/2)/scale)+center_x;
	}
	
	private int screenToDataY(int y)
	{
		return (int)((y-painter.getHeight()/2)/scale)+center_y;
	}
	
	private void addPlanet(int x, int y)
	{
		HashSet<Satellite> sats = system.getorbiting_objects();
		HashSet<Satellite> new_sats=new HashSet<Satellite>();
		if(!(sats instanceof HashSet))
			sats=new HashSet<Satellite>();
		
		for(Satellite sat : sats)
			new_sats.add(sat);
		
		Planet theplanet = new Planet(system, "", (long)0, DEFAULT_PLANET_SIZE, DEFAULT_PLANET_MASS, (byte)0);
		theplanet.orbit = new Orbit((Satellite)theplanet, (Positioning)system, x, y, x, y, 1);
		new_sats.add(theplanet);
		system.setOrbiting_objects(new_sats);
		
		selected_obj = theplanet;
		wait_to_add = ADD_FOCUS;
		drawSystem();
	}
	
	private void addMoon(int x, int y)
	{
		Moon themoon = new Moon(DEFAULT_MOON_MASS, "", DEFAULT_MOON_SIZE);
		themoon.orbit = new Orbit((Satellite)themoon, (Positioning)selected_obj,x,y,x,y,1);
		((Planet)selected_obj).satellites.add(themoon);
		
		selected_obj=themoon;
		wait_to_add=ADD_FOCUS;
		drawSystem();
	}
	
	private boolean locationStarSuitable(StellarObject obj, int x, int y)
	{
		return (Math.hypot(x - painter.getWidth()/2, y-painter.getHeight()/2) <= DEFAULT_STAR_ZONE_SIZE-obj.size/2);
	}
	
	private boolean locationStarSuitable(int x, int y)
	{
		//System.out.println("(" + Integer.toString(x) + "," + Integer.toString(y) + ")");
		return (Math.hypot(x - painter.getWidth()/2, y-painter.getHeight()/2) <= DEFAULT_STAR_ZONE_SIZE-DEFAULT_STAR_SIZE/2);
	}
	
	public void mouseClicked(MouseEvent e)
	{
		painter.requestFocusInWindow();
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e)
	{
		move_center_x_speed=0;
		move_center_y_speed=0;
		recenter_task.cancel();
		recenter_task = new centerMover();
	}
	//end mouselistener code
	
	//mouse motion listener code
	public void mouseDragged(MouseEvent e)
	{
		painter.setCursor(Cursor.getDefaultCursor());
		if(selected_obj instanceof Selectable && drag_start)
		{
			if(selected_obj instanceof Star)
			{
				if(locationStarSuitable((Star)selected_obj,screenToDataX(e.getX()),screenToDataY(e.getY())))
				{
					((Star)selected_obj).x = screenToDataX(e.getX());
					((Star)selected_obj).y = screenToDataY(e.getY());
					drawSystem();
				}
				else
				{
					((Star)selected_obj).x = (int)((screenToDataX(e.getX())-painter.getWidth()/2)/Math.hypot(screenToDataX(e.getX())-painter.getWidth()/2, screenToDataY(e.getY())-painter.getHeight()/2)*(50-((StellarObject)selected_obj).size/2) + painter.getWidth()/2);
					((Star)selected_obj).y = (int)((screenToDataY(e.getY())-painter.getHeight()/2)/Math.hypot(screenToDataX(e.getX())-painter.getWidth()/2, screenToDataY(e.getY())-painter.getHeight()/2)*(50-((StellarObject)selected_obj).size/2) + painter.getHeight()/2);
					drawSystem();
				}
			}
			else if(selected_obj instanceof Satellite) //picks up planets, moons and asteroids here
			{
				((Satellite)selected_obj).orbit.cur_x = screenToDataX(e.getX())-((Satellite)selected_obj).orbit.boss.absoluteCurX();
				((Satellite)selected_obj).orbit.cur_y = screenToDataY(e.getY())-((Satellite)selected_obj).orbit.boss.absoluteCurY();
				((Satellite)selected_obj).orbit.init_x = screenToDataX(e.getX())-((Satellite)selected_obj).orbit.boss.absoluteInitX();
				((Satellite)selected_obj).orbit.init_y = screenToDataY(e.getY())-((Satellite)selected_obj).orbit.boss.absoluteInitY();
				((Satellite)selected_obj).orbit.calculateOrbit();
				drawSystem();
			} else {
				//selected_obj is a Focus!
				((Focus)selected_obj).setX(screenToDataX(e.getX())-((Focus)selected_obj).owner.boss.absoluteCurX());
				((Focus)selected_obj).setY(screenToDataY(e.getY())-((Focus)selected_obj).owner.boss.absoluteCurY());
				((Focus)selected_obj).owner.calculateOrbit();
				drawSystem();
			}
		}
	}
	
	public void mouseMoved(MouseEvent e)
	{
		if(wait_to_add != ADD_NOTHING)
		{
			switch(wait_to_add)
			{
				case ADD_STAR:
					if(locationStarSuitable(screenToDataX(e.getX()), screenToDataY(e.getY())))
						painter.paintGhostObj(system, selected_obj, screenToDataX(e.getX()), screenToDataY(e.getY()), DEFAULT_STAR_SIZE, center_x, center_y, scale);
					else
						drawSystem();
					break;
				case ADD_FOCUS:
					((Satellite)selected_obj).orbit.focus2.setX(screenToDataX(e.getX())-((Satellite)selected_obj).orbit.boss.absoluteInitX());
					((Satellite)selected_obj).orbit.focus2.setY(screenToDataY(e.getY())-((Satellite)selected_obj).orbit.boss.absoluteInitY());
					((Satellite)selected_obj).orbit.calculateOrbit();
					drawSystem();
					break;
			}
		}

		boolean previously_moving = true;
		if(move_center_x_speed==0 && move_center_y_speed==0)
			previously_moving = false;
		
		//sets the speed for camera motion
		if(EDGE_BOUND<e.getX() && e.getX()<painter.getWidth()-EDGE_BOUND)
			move_center_x_speed=0;
		else if(e.getX()>painter.getWidth()-EDGE_BOUND && e.getX()<=painter.getWidth())
			move_center_x_speed=e.getX()-painter.getWidth()+EDGE_BOUND;
		else if(e.getX()< EDGE_BOUND && e.getX() >=0)
			move_center_x_speed=e.getX()-EDGE_BOUND;

		if(EDGE_BOUND<e.getY() && e.getY()<painter.getHeight()-EDGE_BOUND)
			move_center_y_speed=0;
		else if(e.getY()>painter.getHeight()-EDGE_BOUND && e.getY()<=painter.getHeight())
			move_center_y_speed=e.getY()-painter.getHeight()+EDGE_BOUND;
		else if(e.getY()< EDGE_BOUND && e.getY() >=0)
			move_center_y_speed=e.getY()-EDGE_BOUND;
		
		if(move_center_x_speed==0 && move_center_y_speed==0){//now not moving
			recenter_task.cancel();
			recenter_task = new centerMover();
		}
		else if(!previously_moving)
			camera_timer.schedule(recenter_task, 200, 20); //task, then delay, then period
		
		//Set the cursor
		if(move_center_x_speed > 0){
			if(move_center_y_speed > 0)
				painter.setCursor(down_right_arrow);
			else if(move_center_y_speed < 0)
				painter.setCursor(up_right_arrow);
			else
				painter.setCursor(right_arrow);
		}
		else if(move_center_x_speed < 0){
			if(move_center_y_speed > 0)
				painter.setCursor(down_left_arrow);
			else if(move_center_y_speed < 0)
				painter.setCursor(up_left_arrow);
			else
				painter.setCursor(left_arrow);
		}
		else{ //if move_center_x_speed equals 0
			if(move_center_y_speed > 0)
				painter.setCursor(down_arrow);
			else if(move_center_y_speed < 0)
				painter.setCursor(up_arrow);
			else
				painter.setCursor(Cursor.getDefaultCursor());
		}
	}
	//end mouse motion listener code
	
	private void drawSystem()
	{
		system.setWidth(painter.getWidth());
		system.setHeight(painter.getHeight());
		painter.paintSystem(system, selected_obj, center_x, center_y, scale);
	}
	
	private void deleteSelected()
	{
		if(selected_obj instanceof Star)
		{
			system.stars.remove(selected_obj);
			recalculateOrbits();
		}
		else //selected_obj is a satellite
		{
			if(selected_obj instanceof Planet || selected_obj instanceof Asteroid)
			{
				for(Satellite orbiting : system.orbiting_objects)
				{
					if(selected_obj == orbiting)
					{
						system.orbiting_objects.remove(orbiting);
						break;
					}
				}
			}
			else //search for moons and stations
			{
				for(Satellite orbiting : system.orbiting_objects)
				{
					if(orbiting instanceof Planet && (selected_obj instanceof Moon))
					{
						Planet cur_planet=(Planet)orbiting;
						for(Satellite sat : cur_planet.satellites)
						{
							if(selected_obj == sat)
								cur_planet.satellites.remove(sat);
						}
					}
					else if(orbiting == selected_obj)
						system.orbiting_objects.remove(orbiting);
				}
			}
		}
		
		selected_obj=null;
		ObjectSelected(false);
		drawSystem();
		setUndoPoint();
	}
	
	Action delete_action = new AbstractAction()
	{
		public void actionPerformed(ActionEvent e)
		{
			if(selected_obj instanceof StellarObject)
				deleteSelected();
		}
	};
	
	private int minimumObjectSize(StellarObject obj)
	{
		if(obj instanceof Planet)
			return GalacticStrategyConstants.MIN_PLANET_SIZE;
		else if(obj instanceof Star)
			return GalacticStrategyConstants.MIN_STAR_SIZE;
		else if(obj instanceof Moon)
			return GalacticStrategyConstants.MIN_MOON_SIZE;
		else
			return GalacticStrategyConstants.MIN_ASTEROID_SIZE;
	}
	
	private int maximumObjectSize(StellarObject obj)
	{
		if(obj instanceof Star)
			return GalacticStrategyConstants.MAX_STAR_SIZE;
		else if(obj instanceof Planet)
			return GalacticStrategyConstants.MAX_PLANET_SIZE;
		else if(obj instanceof Moon)
			return GalacticStrategyConstants.MAX_MOON_SIZE;
		else
			return GalacticStrategyConstants.MAX_ASTEROID_SIZE;
	}
	
	private class editDialog extends JDialog implements ActionListener, ChangeListener
	{
		private editDialog()
		{
			super(frame, "Edit Object", true);
			
			JPanel master=new JPanel();
			
			if(selected_obj instanceof Star)
			{
				String[] color_options={"Choose a Color", "Red", "Orange", "Yellow", "White", "Blue"};
				JComboBox box = new JComboBox(color_options);
				box.setSelectedItem(color_options[((Star)selected_obj).color]);
				box.addActionListener(this);
				master.add(box);
			}
			else if(selected_obj instanceof Planet)
			{
			}
			
			JSlider resizer = new JSlider(JSlider.HORIZONTAL, minimumObjectSize((StellarObject)selected_obj),maximumObjectSize((StellarObject)selected_obj), ((StellarObject)selected_obj).size);
			resizer.setMajorTickSpacing(GalacticStrategyConstants.MAJOR_TICKS_FOR_OBJECT_SIZE);
			resizer.setMinorTickSpacing(GalacticStrategyConstants.MINOR_TICKS_FOR_OBJECT_SIZE);
			resizer.setPaintTicks(true);
			resizer.addChangeListener(this);
			master.add(resizer);
			
			JButton close_but=new JButton("Close");
			close_but.addActionListener(this);
			master.add(close_but);
			
			add(master);
			pack();
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() instanceof JComboBox)
			{
				int color = ((JComboBox)e.getSource()).getSelectedIndex();
				((Star)selected_obj).color=color;
				drawSystem();
			}
			else if(e.getSource() instanceof JButton)
				dispose();
		}
		
		public void stateChanged(ChangeEvent e)
		{
			((StellarObject)selected_obj).size = ((JSlider)e.getSource()).getValue();
			drawSystem();
		}
	}
	
	private void editSelected(){new editDialog();}
	
	public void windowClosing(WindowEvent e)
	{
		if(TC instanceof TimeControl)
		{
			TC.stopTask();
			TimeUpdater(0);
		}
	}
	
	public void windowClosed(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowActivated(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}