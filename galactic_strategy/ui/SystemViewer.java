package galactic_strategy.ui;
import galactic_strategy.Constants;
import galactic_strategy.TaskManager;
import galactic_strategy.UndoRedoStack;
import galactic_strategy.game_objects.Asteroid;
import galactic_strategy.game_objects.Focus;
import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.MathFormula;
import galactic_strategy.game_objects.Moon;
import galactic_strategy.game_objects.Orbit;
import galactic_strategy.game_objects.Orbitable;
import galactic_strategy.game_objects.OwnableSatelliteType;
import galactic_strategy.game_objects.Planet;
import galactic_strategy.game_objects.Satellite;
import galactic_strategy.game_objects.Star;
import galactic_strategy.game_objects.StellarObject;
import galactic_strategy.sync_engine.TimeControl;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

public class SystemViewer extends JDialog implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, WindowListener
{
	static int DEFAULT_STAR_SIZE=Constants.DEFAULT_STAR_SIZE;
	static int DEFAULT_STAR_ZONE_SIZE=Constants.DEFAULT_STAR_ZONE_SIZE;
	static int DEFAULT_PLANET_SIZE = Constants.DEFAULT_PLANET_SIZE;
	static int DEFAULT_MOON_SIZE = Constants.DEFAULT_MOON_SIZE;
	static double DEFAULT_PLANET_MASS = Constants.DEFAULT_PLANET_MASS;
	static double DEFAULT_STAR_MASS = Constants.DEFAULT_STAR_MASS;
	static double DEFAULT_MOON_MASS= Constants.DEFAULT_MOON_MASS;
	
	public enum AddSystem{
		ADD_NOTHING,
		ADD_STAR,
		ADD_PLANET,
		ADD_ASTEROID,
		ADD_MOON,
		ADD_FOCUS,
		RECENTER;
	}
	
	static int EDGE_BOUND=Constants.EDGE_BOUND; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static int SYS_WIDTH=Constants.SYS_WIDTH; //the allowed width of a system
	static int SYS_HEIGHT=Constants.SYS_HEIGHT; //the allowed height of a system
	
	//sets parameters for scrolling the system view around
	int move_center_x_speed = 0; //left is negative, right is positive
	int move_center_y_speed = 0; //up is negative, down is positive
	
	AddSystem wait_to_add = AddSystem.ADD_NOTHING;
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
	
	// keeps track of the type of the next planet to create (between clicking add and placing the planet)
	private OwnableSatelliteType planet_type;
	
	//objects used to track time flow for time simulation
	TimeControl TC;
	TaskManager TM;
	UpdateTask task;
	java.util.Timer camera_timer;
	centerMover recenter_task;
	
	//sets up the viewpoint for the system
	int center_x;
	int center_y;
	double scale=Constants.DEFAULT_SCALE;
	
	//sets up custom cursors
	MoveScreenCursors cursors;
	
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
		
		//set up custom cursors
		cursors = new MoveScreenCursors();
		
		//it is necessary to invoke this later because before the systemviewer is setvisible it has no height and width, which drawSystem() uses to determine the height/width of GSystem, which is then referenced in absoluteCurX/CurY/InitX/InitY to determine where the center of the system is for coordinate purposes.
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			center_x=painter.getWidth()/2;
			center_y=painter.getHeight()/2;
			drawSystem();}});
		
		setVisible(true);
	}
	
	public <T> java.util.List<T> wrapInList(T obj)
	{
		List<T> the_set = new ArrayList<T>();
		if(obj != null)
			the_set.add(obj);
		return the_set;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==t_toggle)
			painter.paintSystem(system, wrapInList(selected_obj), !painter.design_view, center_x, center_y, scale, null);
		else if(e.getSource()==t_add)
		{
			String obj_to_add;
			Set<Star> stars = system.getStars();
			if(stars == null || stars.size() == 0)
			{
				obj_to_add="Star";
			}
			else if(!(selected_obj instanceof Planet))
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
				wait_to_add=AddSystem.ADD_STAR;
			else if(obj_to_add=="Planet"){
				wait_to_add=AddSystem.ADD_PLANET;
				planet_type = choosePlanetType();
			}
			else if(obj_to_add=="Asteroid")
				wait_to_add=AddSystem.ADD_ASTEROID;
			else if(obj_to_add=="Moon")
				wait_to_add=AddSystem.ADD_MOON;
			
			t_recenter.setEnabled(false);
			c_recenter.setEnabled(false);
		}
		else if(e.getSource() == c_add) //here, the placement of the system has already been determined by a mouse click, and stored in c_x and c_y
		{
			String obj_to_add;

			boolean star_suitable = locationStarSuitable(screenToDataX(c_x),screenToDataY(c_y));
			Set<Star> stars = system.getStars();
			if(stars == null || stars.size() == 0)
			{
				if(star_suitable)
					obj_to_add="Star";
				else
				{
					JOptionPane.showMessageDialog(this, "You must add a star first.", "Cannot add here", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else
			{
				//Moons and stars may only be added under certain conditions.  For moons, that condition is that the planet they will orbit is already selected.
				//For stars, that condition is that the location is "star suitable."  The if statements take care of these conditions
				if(!(selected_obj instanceof Planet))
				{
					if(star_suitable)
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
					if(star_suitable)
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
			}
			
			if(obj_to_add=="Star")
			{
				if(!addStar(screenToDataX(c_x), screenToDataY(c_y)))
					JOptionPane.showMessageDialog(this, "You can't place a star there!", "Invalid star location", JOptionPane.ERROR_MESSAGE);
			}
			else if(obj_to_add=="Planet") {
				OwnableSatelliteType planet_type = choosePlanetType();
				addPlanet(planet_type, screenToDataX(c_x),screenToDataY(c_y));
			}
			//else if(obj_to_add=="Asteroid")
			//	;//no routine yet
			else if(obj_to_add=="Moon")
				addMoon(screenToDataX(c_x),screenToDataY(c_y));
			if(wait_to_add != AddSystem.ADD_FOCUS)
				wait_to_add=AddSystem.ADD_NOTHING;
			drawSystem();
		}
		else if(e.getSource()==t_delete || e.getSource() == c_delete)
			deleteSelected();
		else if(e.getSource()==t_edit || e.getSource()==c_edit)
			editSelected();
		else if(e.getSource()==t_recenter)
		{
			wait_to_add=AddSystem.RECENTER;
		}
		else if(e.getSource() == c_recenter)
		{
			setCenter(screenToDataX(c_x),screenToDataY(c_y));
		}
		else if(e.getSource()==t_time || e.getSource()==c_time)
		{
			if(TC != null)
				TC.resetTime(0);
			else
				TC = new TimeControl(0);

			if(TM == null)
				TM = new TaskManager();
			TM.startConstIntervalTask(new UpdateTask(), 20);
		}
		else if(e.getSource()==t_reset_time || e.getSource() == c_reset_time)
		{
			//clear timer
			if(TM != null)
			{
				TM.stopTask();
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
		if(system.getOrbiting() != null)
		{
			for(Satellite<?> sat : system.getOrbiting())
			{
				//update orbit of sat
				sat.getOrbit().move(time);
				if(sat instanceof Planet)
				{
					if(((Planet)sat).getOrbiting() != null)
					{
						for(Satellite<?> sat2 : ((Planet)sat).getOrbiting())
						{
							//update orbit of sat2
							sat2.getOrbit().move(time);
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
		//search for stars, then orbiting objects and their orbiting
		final double OBJ_TOL = Constants.SELECTION_TOLERANCE/scale; //tolerance
		
		HashSet<Star> stars = system.getStars();
		if(stars != null)
		{
			for(Star st : stars)
			{
				//search for star...
				if(st.shouldSelect(x, y, OBJ_TOL))
				{
					selected_obj = st;
					return;
				}
			}
		}
		
		//search orbiting planets/objects
		ArrayList<Satellite<?>> orbiting_sats = system.getOrbiting();
		if(orbiting_sats != null)
		{
			for(Satellite<?> orbiting : orbiting_sats)
			{
				//search for orbiting...
				if(orbiting.shouldSelect(x, y, OBJ_TOL))
				{
					selected_obj = orbiting;
					return;
				}
				
				if(orbiting instanceof Planet)
				{
					Planet cur_planet = (Planet)orbiting;
					List<Satellite<?>> orbiting2 = cur_planet.getOrbiting();
					if (orbiting2 != null) {
						for(Satellite<?> sat : orbiting2)
						{
							if(sat.absoluteCurX()-OBJ_TOL <= x && x <= sat.absoluteCurX()+OBJ_TOL && sat.absoluteCurY()-OBJ_TOL <= y && y <= sat.absoluteCurY()+OBJ_TOL)
							{
								selected_obj = sat;
								return;
							}
						}
					}
				}
			}
			
			if(selected_obj instanceof Satellite<?>) {//check to see if focus2 of the selected planet was just clicked on
				Satellite<?> sat = (Satellite<?>) selected_obj;
				Orbit o = sat.getOrbit();
				synchronized(o)
				{
					Focus focus2 = o.getFocus2();
					if (focus2.shouldSelect(x, y, OBJ_TOL))
					{
						selected_obj = focus2;//select the focus!
						return;
					}
				}
			}
			else if (selected_obj instanceof Focus) //if a focus is selected
			{
				Focus focus = (Focus) selected_obj;
				if(focus.shouldSelect(x, y, OBJ_TOL))
				{
					return; //the Focus is already selected!
				}
			}
		}
		
		throw new NoObjectLocatedException(x, y);
	}
	
	private static class NoObjectLocatedException extends Exception
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
		if(wait_to_add==AddSystem.ADD_NOTHING)
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
			if(wait_to_add==AddSystem.ADD_NOTHING)
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
						addPlanet(planet_type, screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
					case ADD_ASTEROID:
						break;
					case ADD_MOON:
						addMoon(screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
					case ADD_FOCUS:
						wait_to_add=AddSystem.ADD_NOTHING;
						setUndoPoint();
						break;
					case RECENTER:
						setCenter(screenToDataX(e.getX()),screenToDataY(e.getY()));
						break;
				}
				if(wait_to_add != AddSystem.ADD_FOCUS)
				{
					wait_to_add=AddSystem.ADD_NOTHING;
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
		scale -= ((double)e.getWheelRotation())*Constants.SCROLL_SENSITIVITY;
		if(scale < Constants.MIN_SCALE)
			scale = Constants.MIN_SCALE;
		else if(scale > Constants.MAX_SCALE)
			scale = Constants.MAX_SCALE;
		
		//adjust center if part of screen goes out of bounds ////BOOKMARK
		if(screenToDataX(0)<(painter.getWidth()-SYS_WIDTH)/2.0)
			center_x = (int)((painter.getWidth()-SYS_WIDTH)/2.0 + painter.getWidth()/(2.0*scale));//this should be the value of center_x that makes screenToDataX equal to (painter.getWidth()-SYS_WIDTH)/2
		else if(screenToDataX(painter.getWidth())>(painter.getWidth()+SYS_WIDTH)/2)
			center_x = (int)((painter.getWidth()+SYS_WIDTH)/2.0 - painter.getWidth()/(2.0*scale));
		
		if(screenToDataY(0)<(painter.getHeight()-SYS_HEIGHT)/2.0)
			center_y = (int)((painter.getHeight()-SYS_HEIGHT)/2.0 + painter.getHeight()/(2.0*scale));
		else if(screenToDataY(painter.getHeight())>(painter.getHeight()+SYS_HEIGHT)/2.0)
			center_y = (int)((painter.getHeight()+SYS_HEIGHT)/2.0 - painter.getHeight()/(2.0*scale));
		
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
			if (system.getStars() == null)
				system.setStars(new HashSet<Star>());
			Star new_star = new Star("", DEFAULT_STAR_SIZE, DEFAULT_STAR_MASS, ImageResource.NULL_STAR.ordinal(), x, y, system);
			system.getStars().add(new_star);
			
			selected_obj = new_star;
			recalculateOrbits();
		}
		
		drawSystem();
		setUndoPoint();
		return success;
	}
	
	private void recalculateOrbits()
	{
		if(system.getOrbiting() != null)
		{
			for(Satellite<?> sat: system.getOrbiting())
			{
				sat.getOrbit().calculateOrbit();
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
	
	private void addPlanet(OwnableSatelliteType typePlanet, int x, int y)
	{				
		Planet theplanet = new Planet(
				system.getOrbiting().size(), "", 
				(int)MathFormula.randomize(typePlanet.initial_pop,Constants.rand_mod*typePlanet.initial_pop), 
				(int)MathFormula.randomize(typePlanet.pop_capacity,typePlanet.pop_capacity*Constants.rand_mod), 
				DEFAULT_PLANET_SIZE, DEFAULT_PLANET_MASS, typePlanet.PopGrowthRate, typePlanet.building_Num, 
				MathFormula.randomize(typePlanet.mining_rate, typePlanet.mining_rate*Constants.rand_mod)
		);
		theplanet.setOrbit(new Orbit(theplanet, (Orbitable<GSystem>)system, x, y, x, y, Orbit.DIRECTION.CLOCKWISE));
		system.getOrbiting().add(theplanet);
		selected_obj = theplanet;
		wait_to_add = AddSystem.ADD_FOCUS;
		drawSystem();
	}

	private OwnableSatelliteType choosePlanetType() {
		OwnableSatelliteType typePlanet = null;
		
		List<String> new_options = new ArrayList<String>();
		for (OwnableSatelliteType type : OwnableSatelliteType.values()) {
			if (type != OwnableSatelliteType.Moon) {
				new_options.add(type.namePlanet);
			}
		}
		String planet_type = (String)JOptionPane.showInputDialog(
			this,
			"Select the type of Planet to add",
			"Add Planet",
			JOptionPane.QUESTION_MESSAGE,
			null,
			new_options.toArray(),
			new_options.get(0)
		);
		
		for (OwnableSatelliteType type : OwnableSatelliteType.values()) {
			if (planet_type == type.namePlanet) {
				typePlanet = type;
				break;
			}
		}
		assert typePlanet != null;
		return typePlanet;
	}
	
	private void addMoon(int x, int y)
	{
		Moon themoon = new Moon(((Planet)selected_obj).getOrbiting().size(), DEFAULT_MOON_MASS, "", DEFAULT_MOON_SIZE);
		themoon.setOrbit(new Orbit((Satellite<Moon>)themoon, (Orbitable<Planet>)selected_obj,x,y,x,y, Orbit.DIRECTION.CLOCKWISE));
		((Planet)selected_obj).getOrbiting().add(themoon);
		
		selected_obj = themoon;
		wait_to_add=AddSystem.ADD_FOCUS;
		drawSystem();
	}
	
	private boolean locationStarSuitable(StellarObject obj, int x, int y)
	{
		return (Math.hypot(x - painter.getWidth()/2, y-painter.getHeight()/2) <= DEFAULT_STAR_ZONE_SIZE-obj.getSize()/2);
	}
	
	private boolean locationStarSuitable(int x, int y)
	{
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
		if(selected_obj != null && drag_start)
		{
			if(selected_obj instanceof Star)
			{
				Star star = (Star) selected_obj;
				if(locationStarSuitable(star, screenToDataX(e.getX()), screenToDataY(e.getY())))
				{
					star.setX(screenToDataX(e.getX()));
					star.setY(screenToDataY(e.getY()));
					drawSystem();
				}
				else
				{
					star.setX((int)((screenToDataX(e.getX())-painter.getWidth()/2.0)/Math.hypot(screenToDataX(e.getX())-painter.getWidth()/2, screenToDataY(e.getY())-painter.getHeight()/2.0)*(Constants.DEFAULT_STAR_ZONE_SIZE-star.getSize()/2.0) + painter.getWidth()/2.0));
					star.setY((int)((screenToDataY(e.getY())-painter.getHeight()/2.0)/Math.hypot(screenToDataX(e.getX())-painter.getWidth()/2, screenToDataY(e.getY())-painter.getHeight()/2.0)*(Constants.DEFAULT_STAR_ZONE_SIZE-star.getSize()/2.0) + painter.getHeight()/2.0));
					drawSystem();
				}
			}
			else if(selected_obj instanceof Satellite<?>) //picks up planets, moons and asteroids here
			{
				Orbit o = ((Satellite<?>) selected_obj).getOrbit();
				Orbitable<?> boss = o.getBoss();
				o.setCur_x(screenToDataX(e.getX())-boss.absoluteCurX());
				o.setCur_y(screenToDataY(e.getY())-boss.absoluteCurY());
				o.setInit_x(screenToDataX(e.getX())-boss.absoluteInitX());
				o.setInit_y(screenToDataY(e.getY())-boss.absoluteInitY());
				o.calculateOrbit();
				drawSystem();
			} else {
				//selected_obj is a Focus!
				Focus f = (Focus) selected_obj;
				Orbit owner = f.getOwner();
				Orbitable<?> boss = owner.getBoss();
				f.setX(screenToDataX(e.getX())- boss.absoluteCurX());
				f.setY(screenToDataY(e.getY())- boss.absoluteCurY());
				owner.calculateOrbit();
				drawSystem();
			}
		}
	}
	
	public void mouseMoved(MouseEvent e)
	{
		if(wait_to_add != AddSystem.ADD_NOTHING)
		{
			switch(wait_to_add)
			{
				case ADD_STAR:
					if(locationStarSuitable(screenToDataX(e.getX()), screenToDataY(e.getY())))
						painter.paintGhostObj(system, wrapInList(selected_obj), screenToDataX(e.getX()), screenToDataY(e.getY()), DEFAULT_STAR_SIZE, center_x, center_y, scale);
					else
						drawSystem();
					break;
				case ADD_FOCUS:
					Orbit o = ((Satellite<?>)selected_obj).getOrbit();
					o.getFocus2().setX(screenToDataX(e.getX())-o.getBoss().absoluteInitX());
					o.getFocus2().setY(screenToDataY(e.getY())-o.getBoss().absoluteInitY());
					o.calculateOrbit();
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
				painter.setCursor(cursors.downRight());
			else if(move_center_y_speed < 0)
				painter.setCursor(cursors.upRight());
			else
				painter.setCursor(cursors.right());
		}
		else if(move_center_x_speed < 0){
			if(move_center_y_speed > 0)
				painter.setCursor(cursors.downLeft());
			else if(move_center_y_speed < 0)
				painter.setCursor(cursors.upLeft());
			else
				painter.setCursor(cursors.left());
		}
		else{ //if move_center_x_speed equals 0
			if(move_center_y_speed > 0)
				painter.setCursor(cursors.down());
			else if(move_center_y_speed < 0)
				painter.setCursor(cursors.up());
			else
				painter.setCursor(Cursor.getDefaultCursor());
		}
	}
	//end mouse motion listener code
	
	private void drawSystem()
	{
		system.setWidth(painter.getWidth());
		system.setHeight(painter.getHeight());
		painter.paintSystem(system, wrapInList(selected_obj), center_x, center_y, scale, null);
	}
	
	private void deleteSelected()
	{
		if(selected_obj instanceof Star)
		{
			system.getStars().remove(selected_obj);
			recalculateOrbits();
		}
		else //selected_obj is a satellite
		{
			if(selected_obj instanceof Planet || selected_obj instanceof Asteroid)
			{
				for(Satellite<?> orbiting : system.getOrbiting())
				{
					if(selected_obj == orbiting)
					{
						system.getOrbiting().remove(orbiting);
						break;
					}
				}
			}
			else //search for moons and stations
			{
				for(Satellite<?> orbiting : system.getOrbiting())
				{
					if(orbiting instanceof Planet && (selected_obj instanceof Moon))
					{
						Planet cur_planet=(Planet)orbiting;
						for(Satellite<?> sat : cur_planet.getOrbiting())
						{
							if(selected_obj == sat)
								cur_planet.getOrbiting().remove(sat);
						}
					}
					else if(orbiting == selected_obj)
						system.getOrbiting().remove(orbiting);
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
			return Constants.MIN_PLANET_SIZE;
		else if(obj instanceof Star)
			return Constants.MIN_STAR_SIZE;
		else if(obj instanceof Moon)
			return Constants.MIN_MOON_SIZE;
		else
			return Constants.MIN_ASTEROID_SIZE;
	}
	
	private int maximumObjectSize(StellarObject obj)
	{
		if(obj instanceof Star)
			return Constants.MAX_STAR_SIZE;
		else if(obj instanceof Planet)
			return Constants.MAX_PLANET_SIZE;
		else if(obj instanceof Moon)
			return Constants.MAX_MOON_SIZE;
		else
			return Constants.MAX_ASTEROID_SIZE;
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
				JComboBox<String> box = new JComboBox<String>(color_options);
				box.setSelectedItem(color_options[((Star)selected_obj).getPicture_num()]);
				box.addActionListener(this);
				master.add(box);
			}
			else if(selected_obj instanceof Planet)
			{
			}
			
			JSlider resizer = new JSlider(
				JSlider.HORIZONTAL,
				minimumObjectSize((StellarObject)selected_obj),
				maximumObjectSize((StellarObject)selected_obj),
				(int)((StellarObject)selected_obj).getSize()
			);
			resizer.setMajorTickSpacing(Constants.MAJOR_TICKS_FOR_OBJECT_SIZE);
			resizer.setMinorTickSpacing(Constants.MINOR_TICKS_FOR_OBJECT_SIZE);
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
				int color = ((JComboBox<?>)e.getSource()).getSelectedIndex();
				((Star)selected_obj).setPicture_num(color);
				drawSystem();
			}
			else if(e.getSource() instanceof JButton)
				dispose();
		}
		
		public void stateChanged(ChangeEvent e)
		{
			((StellarObject)selected_obj).setSize(((JSlider)e.getSource()).getValue());
			drawSystem();
		}
	}
	
	private void editSelected(){new editDialog();}
	
	public void windowClosing(WindowEvent e)
	{
		if(TM != null)
		{
			TM.stopTask();
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