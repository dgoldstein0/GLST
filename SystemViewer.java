import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class SystemViewer extends JDialog implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	static int DEFAULT_STAR_SIZE=25;
	static int DEFAULT_STAR_ZONE_SIZE=50;
	static int DEFAULT_PLANET_SIZE = 10;
	static double DEFAULT_PLANET_MASS = 10;
	static double DEFAULT_STAR_MASS = 10000;
	
	final static int ADD_NOTHING=0;
	final static int ADD_STAR=1;
	final static int ADD_PLANET=2;
	final static int ADD_ASTEROID=3;
	final static int ADD_MOON=4;
	final static int ADD_FOCUS=5;
	final static int RECENTER=6;
	
	int wait_to_add = ADD_NOTHING;
	boolean drag_start;	
	
	GSystem system;
	SystemPainter painter;
	StellarObject selected_obj;
	JFrame frame;
	
	JButton t_toggle;
	JButton t_add;
	JButton t_delete;
	JButton t_edit;
	JButton t_recenter;
	JButton t_time;
	JButton t_reset_time;
	
	TimeControl TC;
	java.util.Timer timer;
	UpdateTask task;
	
	int center_x;
	int center_y;
	double scale=1.0d;
	
	public SystemViewer(JFrame frame, GSystem sys)
	{
		super(frame,"System Viewer",true);
		setResizable(false);
		
		this.frame=frame;
		system=sys;
		
		painter = new SystemPainter(true);
		painter.addMouseListener(this);
		painter.addMouseMotionListener(this);
		painter.addMouseWheelListener(this);
		add(painter, BorderLayout.CENTER);
		
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
		
		pack();
		setSize(800,650);
		

		ObjectSelected(false);
		
		//it is necessary to invoke this later because before the systemviewer is setvisible it has no height and width, which drawSystem() uses to determine the height/width of GSystem, which is then referenced in absoluteCurX/CurY/InitX/InitY to determine where the center of the system is for coordinate purposes.
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			center_x=painter.getWidth()/2;
			center_y=painter.getHeight()/2;
			drawSystem();}});
		
		setVisible(true);//since systemviewer is modal, nothing can come after here.
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==t_toggle)
			painter.paintSystem(system, selected_obj, !painter.design_view, center_x, center_y, scale);
		else if(e.getSource()==t_add)
		{
			String obj_to_add;
			if(system.stars instanceof HashSet)
			{
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
			}
			else
				obj_to_add="Star";
			
			if(obj_to_add=="Star")
				wait_to_add=ADD_STAR;
			else if(obj_to_add=="Planet")
				wait_to_add=ADD_PLANET;
			else if(obj_to_add=="Asteroid")
				wait_to_add=ADD_ASTEROID;
			else if(obj_to_add=="Moon")
				wait_to_add=ADD_MOON;
			t_recenter.setEnabled(false);
		}
		else if(e.getSource()==t_delete)
			deleteSelected();
		else if(e.getSource()==t_edit)
			editSelected();
		else if(e.getSource()==t_recenter)
		{
			wait_to_add=RECENTER;
		}
		else if(e.getSource()==t_time)
		{
			if(TC instanceof TimeControl)
				TC.resetTime();
			else
				TC = new TimeControl();

			TC.startConstIntervalTask(new UpdateTask(),20);
		}
		else if(e.getSource()==t_reset_time)
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
	
	private void ObjectSelected(boolean select)
	{
		t_delete.setEnabled(select);
		t_edit.setEnabled(select);
	}
	
	private void locateObj(int x, int y) throws NoObjectLocatedException
	{
		//search for stars, then orbiting objects and their satellites
		final double OBJ_TOL = 5/scale; //tolerance
		
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
			catch(NoObjectLocatedException x)
			{
				drag_start=false;
			}
		}
	}
	
	public void mouseReleased(MouseEvent e)
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
					break;
				case ADD_FOCUS:
					wait_to_add=ADD_NOTHING;
					break;
				case RECENTER:
					setCenter(screenToDataX(e.getX()),screenToDataY(e.getY()));
					break;
			}
			if(wait_to_add != ADD_FOCUS)
			{
				wait_to_add=ADD_NOTHING;
				t_recenter.setEnabled(true);
			}
			ObjectSelected(true);
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		scale -= ((double)e.getWheelRotation())/10;
		if(scale < 1.0d)
			scale =1.0d;
		else if(scale > 5.0d)
			scale = 5.0d;
		drawSystem();
	}
	
	private void setCenter(int x, int y)
	{
		center_x=x;
		center_y=y;
		drawSystem();
	}
	
	private void addStar(int x, int y)
	{
		if(locationStarSuitable(x, y))
		{
			if (!(system.stars instanceof HashSet))
				system.stars=new HashSet<Star>();
			Star new_star = new Star("", DEFAULT_STAR_SIZE, DEFAULT_STAR_MASS, Star.COLOR_NULL, x, y);
			system.stars.add(new_star);
			
			selected_obj=new_star;
		}
		
		drawSystem();
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
		
		Planet theplanet = new Planet("", (long)0, DEFAULT_PLANET_SIZE, DEFAULT_PLANET_MASS, (byte)0);
		theplanet.orbit = new Orbit((Satellite)theplanet, (Positioning)system, x, y, x, y, 1);
		new_sats.add(theplanet);
		system.setOrbiting_objects(new_sats);
		
		selected_obj = theplanet;
		wait_to_add = ADD_FOCUS;
		drawSystem();
	}
	
	private boolean locationStarSuitable(StellarObject obj, int x, int y)
	{
		return (Math.hypot(x-painter.getWidth()/2, y-painter.getHeight()/2) <= DEFAULT_STAR_ZONE_SIZE-obj.size/2);
	}
	
	private boolean locationStarSuitable(int x, int y)
	{
		return (Math.hypot(x-painter.getWidth()/2, y-painter.getHeight()/2) <= DEFAULT_STAR_ZONE_SIZE-DEFAULT_STAR_SIZE/2);
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	//end mouselistener code
	
	//mouse motion listener code
	public void mouseDragged(MouseEvent e)
	{
		if(selected_obj instanceof StellarObject && drag_start)
		{
			if(selected_obj instanceof Star)
			{
				if(locationStarSuitable(selected_obj,e.getX(),e.getY()))
				{
					((Star)selected_obj).x = e.getX();
					((Star)selected_obj).y = e.getY();
					drawSystem();
				}
				else
				{
					((Star)selected_obj).x = (int)((e.getX()-painter.getWidth()/2)/Math.hypot(e.getX()-painter.getWidth()/2, e.getY()-painter.getHeight()/2)*(50-selected_obj.size/2) + painter.getWidth()/2);
					((Star)selected_obj).y = (int)((e.getY()-painter.getHeight()/2)/Math.hypot(e.getX()-painter.getWidth()/2, e.getY()-painter.getHeight()/2)*(50-selected_obj.size/2) + painter.getHeight()/2);
					drawSystem();
				}
			}
			else if(selected_obj instanceof Planet)
			{
				((Satellite)selected_obj).orbit.cur_x = e.getX()-painter.getWidth()/2;
				((Satellite)selected_obj).orbit.cur_y = e.getY()-painter.getHeight()/2;
				((Satellite)selected_obj).orbit.init_x = e.getX()-painter.getWidth()/2;
				((Satellite)selected_obj).orbit.init_y = e.getY()-painter.getHeight()/2;
				((Satellite)selected_obj).orbit.calculateOrbit();
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
					if(locationStarSuitable(e.getX(), e.getY()))
						painter.paintGhostObj(system, selected_obj, e.getX(), e.getY(), DEFAULT_STAR_SIZE, center_x, center_y, scale);
					else
						drawSystem();
					break;
				case ADD_FOCUS:
					((Satellite)selected_obj).orbit.focus2_x = e.getX()-painter.getWidth()/2;
					((Satellite)selected_obj).orbit.focus2_y = e.getY()-painter.getHeight()/2;
					((Satellite)selected_obj).orbit.calculateOrbit();
					drawSystem();
					break;
			}
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
			system.stars.remove(selected_obj);
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
			return 10;
		else if(obj instanceof Star)
			return 16;
		else if(obj instanceof Satellite)
			return 6;
		return 2;
	}
	
	private int maximumObjectSize(StellarObject obj)
	{
		if(obj instanceof Star)
			return 100;
		if(obj instanceof Planet)
			return 75;
		return 50;
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
			
			JSlider resizer = new JSlider(JSlider.HORIZONTAL, minimumObjectSize(selected_obj),maximumObjectSize(selected_obj), selected_obj.size);
			resizer.setMajorTickSpacing(25);
			resizer.setMinorTickSpacing(5);
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
			selected_obj.size = ((JSlider)e.getSource()).getValue();
			drawSystem();
		}
	}
	
	private void editSelected(){new editDialog();}
}