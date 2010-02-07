import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.beans.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.*;

public class GDFrame implements Runnable, ActionListener, ChangeListener, MouseMotionListener, MouseListener, WindowListener, KeyListener
{
	SimpleNotifier n;//REMOVE
	
	final static int DRAG_NONE=0;
	final static int DRAG_DIST=1;
	final static int DRAG_RANGE=2;
	
	JFrame frame;
	JFileChooser fc;
	File cur_file;
	
	GalacticMapPainter panel;
	
	//file menu items
	JMenuItem new_item;
	JMenuItem open_item;
	JMenuItem close_item;
	JMenuItem save_item;
	JMenuItem saveas_item;
	JMenuItem exit_item;
	
	//Galaxy menu items
	JMenuItem name_gal_item;
	JMenuItem rand_name_sys_item;
	JMenuItem rand_name_satellites_item;
	JMenuItem set_start_loc_item;
	
	//help menu items
	JMenuItem help_item;
	JMenuItem about_item;
	
	//context menu and items
	JPopupMenu context_menu;
	JMenuItem c_add;
	JMenuItem c_range;
	JMenuItem c_edit;
	JMenuItem c_delete;
	JMenuItem c_distance;
	JMenuItem c_nav;
	
	//saves click location which activates context menu, so once an option is selected it is known where the context menu came from
	int c_x;
	int c_y;
	
	//save shifts and alts for click-drag motion, which allows the selection of planets.  shift-drag adds planets to the current selection, whereas alt-drag removes them from selection
	boolean shift_down_on_click;
	boolean alt_down_on_click;
	
	//when a click selects no systems, the button is saved for mouseDrag, which can draw a select box
	int select_but;
	
	int current_nav_level=GalacticStrategyConstants.DEFAULT_NAV_LEVEL;//saves the nav level from n_slide
	
	boolean wait_to_add_sys;//used with t_add so that the next click adds a system to the galactic map
	
	//variables necessary for proper drag-drop drawing
	int drag_options; //keep distances/ranges drawn during dragging.  Uses DRAG_NONE, DRAG_DIST, and DRAG_RANGE
	boolean drag_end; //makes sure that systems aren't deselected after dragging them around
	boolean drag_start; //make sure that you click on the system before you drag it (i.e, you are holding one of the systems)
	
	//these variables are used primarily in multi-system drags to insure that no selected system is dragged off the screen
	int drag_box_right;
	int drag_box_left;
	int drag_box_top;
	int drag_box_bottom;
	
	//Galaxy Designer Toolbar
	JButton t_add;
	JButton t_range;
	JButton t_edit;
	JButton t_delete;
	JButton t_distance;
	JSlider show_dist;
	JSpinner t_nav;
	JSlider n_slide;
	JComboBox t_disp_navs;
		int nav_display=GalacticStrategyConstants.DEFAULT_NAV_OPTIONS; //0=none, 1=selected, 2=all - use next 3 constants
		final static int NAV_DISP_NONE=0;
		final static int NAV_DISP_SELECTED=1;
		final static int NAV_DISP_ALL=2;
	JCheckBox t_disp_unnav_sys;
		boolean display_unnavigable=GalacticStrategyConstants.DEFAULT_DISPLAY_UNNAV;
	
	//open screen
	JDialog open_screen_dialog;
	JButton o_screen_new;
	JButton o_screen_load;
	JButton o_screen_exit;
	
	Galaxy map;//the galactic map info object
	HashSet<GSystem> selected_systems;//the system(s) currently selected
	HashSet<GSystem> possibly_sel_desel_sys;//systems caught in an alt-drag or shift_drag
	UndoRedoStack undostack; //this object handles the undo/redo functionality of the application
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new GDFrame());
	}
	
	public void run()
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		frame=new JFrame("Galaxy Designer");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		frame.setResizable(false);
		
		frame.setSize(800,650);
		
		frame.addKeyListener(this);
		
		//start menubar code/////////////////////////////////////////////////*
		JMenuBar menu_bar=new JMenuBar();
		
		JMenu file_menu=new JMenu("File");
		file_menu.setMnemonic(KeyEvent.VK_F);
		
		new_item=new JMenuItem("New");
		new_item.addActionListener(this);
		new_item.setMnemonic(KeyEvent.VK_N);
		new_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		file_menu.add(new_item);
		
		open_item=new JMenuItem("Open");
		open_item.addActionListener(this);
		open_item.setMnemonic(KeyEvent.VK_O);
		open_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		file_menu.add(open_item);
		
		close_item=new JMenuItem("Close");
		close_item.addActionListener(this);
		close_item.setMnemonic(KeyEvent.VK_C);
		close_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
		file_menu.add(close_item);
		
		save_item=new JMenuItem("Save");
		save_item.addActionListener(this);
		save_item.setMnemonic(KeyEvent.VK_S);
		save_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		file_menu.add(save_item);
		
		saveas_item=new JMenuItem("Save As...");
		saveas_item.addActionListener(this);
		saveas_item.setMnemonic(KeyEvent.VK_A);
		file_menu.add(saveas_item);
		
		file_menu.add(new JSeparator());
		
		exit_item=new JMenuItem("Exit");
		exit_item.addActionListener(this);
		exit_item.setMnemonic(KeyEvent.VK_X);
		file_menu.add(exit_item);
		
		menu_bar.add(file_menu);
		
		JMenu galaxy_menu = new JMenu("Galaxy");
		galaxy_menu.setMnemonic(KeyEvent.VK_G);
		
		name_gal_item = new JMenuItem("Name the Galaxy");
		name_gal_item.addActionListener(this);
		name_gal_item.setMnemonic(KeyEvent.VK_N);
		galaxy_menu.add(name_gal_item);
		
		rand_name_sys_item = new JMenuItem("Randomize Names for Unnamed Systems");
		rand_name_sys_item.addActionListener(this);
		rand_name_sys_item.setMnemonic(KeyEvent.VK_R);
		galaxy_menu.add(rand_name_sys_item);
		
		rand_name_satellites_item = new JMenuItem("Randomize Names for Unnamed Satellites");
		rand_name_satellites_item.addActionListener(this);
		rand_name_satellites_item.setMnemonic(KeyEvent.VK_N);
		galaxy_menu.add(rand_name_satellites_item);
		
		set_start_loc_item = new JMenuItem("Set Start Locations");
		set_start_loc_item.addActionListener(this);
		set_start_loc_item.setMnemonic(KeyEvent.VK_S);
		galaxy_menu.add(set_start_loc_item);
		
		menu_bar.add(galaxy_menu);
		
		JMenu help_menu=new JMenu("Help");
		help_menu.setMnemonic(KeyEvent.VK_H);
		
		help_item=new JMenuItem("Help");
		help_item.addActionListener(this);
		help_item.setMnemonic(KeyEvent.VK_H);
		help_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help_menu.add(help_item);
		
		about_item=new JMenuItem("About");
		about_item.addActionListener(this);
		about_item.setMnemonic(KeyEvent.VK_A);
		help_menu.add(about_item);
		
		menu_bar.add(help_menu);	
		
		frame.setJMenuBar(menu_bar);
		//end menubar code///////////////////////////////////////////////////*
		
		
		
		
		
		panel=new GalacticMapPainter();
		panel.setMinimumSize(new Dimension(800,600));
		panel.setPreferredSize(new Dimension(800,600));
		panel.setMaximumSize(new Dimension(800,600));
		n = new SimpleNotifier();//REMOVE
		panel.add(n);
		
		frame.add(panel, BorderLayout.CENTER);
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"delete");
		panel.getActionMap().put("delete", delete_action);
		
		panel.addMouseMotionListener(this);
		panel.addKeyListener(this);
		panel.addMouseListener(this);
		
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
		
		c_range = new JMenuItem("Show Range");
		c_range.addActionListener(this);
		context_menu.add(c_range);
		
		c_distance=new JMenuItem("Show Distances");
		c_distance.addActionListener(this);
		context_menu.add(c_distance);
		
		c_nav=new JMenuItem("Set Navigability");
		c_nav.addActionListener(this);
		context_menu.add(c_nav);
		//end context menu code///////////////////
		
		//start toolbar code/////////////
		JToolBar toolbar=new JToolBar("Galaxy Designer Tool Bar");
		
		t_add=new JButton("Add");
		t_add.setMnemonic(KeyEvent.VK_A);
		t_add.setToolTipText("add a system");
		t_add.addActionListener(this);
		toolbar.add(t_add);
		
		t_edit=new JButton("Edit");
		t_edit.setToolTipText("edit the selected system");
		t_edit.setMnemonic(KeyEvent.VK_E);
		t_edit.addActionListener(this);
		toolbar.add(t_edit);
		
		t_delete=new JButton("Delete");
		t_delete.setToolTipText("delete the selected system");
		t_delete.setMnemonic(KeyEvent.VK_D);
		t_delete.addActionListener(this);
		toolbar.add(t_delete);
		
		toolbar.add(new JToolBar.Separator());
		
		JPanel dist_panel=new JPanel(new GridLayout(2,1));
		
		t_range=new JButton("Range");
		t_range.setToolTipText("display a range around the selected system");
		t_range.setMnemonic(KeyEvent.VK_R);
		t_range.addActionListener(this);
		dist_panel.add(t_range);
		
		t_distance=new JButton("Distances");
		t_distance.setToolTipText("show the distances between systems");
		t_distance.setMnemonic(KeyEvent.VK_S);
		t_distance.addActionListener(this);
		dist_panel.add(t_distance);
		
		dist_panel.setMaximumSize(new Dimension((int)(dist_panel.getMinimumSize().getWidth()), (int)(dist_panel.getMinimumSize().getHeight())));
		toolbar.add(dist_panel);
		
		show_dist= new JSlider(JSlider.HORIZONTAL, 0, GalacticStrategyConstants.MAX_DIST, GalacticStrategyConstants.DEFAULT_DIST);
		show_dist.addChangeListener(this);
		show_dist.setMajorTickSpacing(GalacticStrategyConstants.DIST_MAJOR_TICKS);
		show_dist.setMinorTickSpacing(GalacticStrategyConstants.DIST_MINOR_TICKS);
		show_dist.setPaintTicks(true);
		show_dist.setBorder(BorderFactory.createTitledBorder("Distance and Range Limit"));
		
		toolbar.add(show_dist);
		
		toolbar.add(new JToolBar.Separator());
		
		n_slide=new JSlider(JSlider.HORIZONTAL, GalacticStrategyConstants.MIN_NAV_LEVEL, GalacticStrategyConstants.MAX_NAV_LEVEL, GalacticStrategyConstants.DEFAULT_NAV_LEVEL);
		n_slide.addChangeListener(this);
		n_slide.setMinorTickSpacing(GalacticStrategyConstants.NAV_LEVEL_TICK_SPACING);
		n_slide.setPaintTicks(true);
		n_slide.setSnapToTicks(true);
		//n_slide.setMaximumSize(new Dimension(200,200));
		n_slide.setToolTipText("Navigability Filter");
		n_slide.setBorder(BorderFactory.createTitledBorder("Filter by Nav"));
		toolbar.add(n_slide);
		
		JPanel nav_panel=new JPanel();
		nav_panel.setBorder(BorderFactory.createTitledBorder("Set Nav"));
		SpinnerModel nav_model=new SpinnerNumberModel(GalacticStrategyConstants.DEFAULT_NAV_LEVEL, GalacticStrategyConstants.MIN_NAV_LEVEL, GalacticStrategyConstants.MAX_NAV_LEVEL, GalacticStrategyConstants.NAV_LEVEL_TICK_SPACING);//******************************BOOKMARK
		t_nav=new JSpinner(nav_model);
		t_nav.addChangeListener(this);
		nav_panel.add(t_nav);
		toolbar.add(nav_panel);
		
		toolbar.add(nav_panel);
		
		JPanel nav_views = new JPanel(new GridLayout(2,1));
		
		JPanel nav_ops_panel=new JPanel();
		Dimension small=nav_ops_panel.getMinimumSize();
		small.setSize(small.getWidth()+2, small.getHeight()+20);
		nav_ops_panel.setPreferredSize(small);
		
		nav_ops_panel.add(new JLabel("Display:"));
		
		String[] nav_options = {"none", "selected", "all"};//do not change order without changing NAV_DISP_ constants
		t_disp_navs = new JComboBox(nav_options);
		t_disp_navs.setSelectedIndex(GalacticStrategyConstants.DEFAULT_NAV_OPTIONS);
		t_disp_navs.setToolTipText("Display navigabilities next to each system");
		t_disp_navs.addActionListener(this);
		nav_ops_panel.add(t_disp_navs);
		nav_views.add(nav_ops_panel);
		
		t_disp_unnav_sys=new JCheckBox("Show unnavigable systems");
		t_disp_unnav_sys.setMnemonic(KeyEvent.VK_U);
		t_disp_unnav_sys.setToolTipText("Toggles the display of systems with navigabilities less than the filter");
		t_disp_unnav_sys.addActionListener(this);
		nav_views.add(t_disp_unnav_sys);
		
		toolbar.add(nav_views);
		
		frame.add(toolbar, BorderLayout.NORTH);
		//end toolbar code///////////////
		
		
		fileIsNotOpen();
		
		frame.pack();
		frame.setVisible(true);
		
		//set up file chooser for opening and saving
		fc=new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		//Create open screen dialog - the dialog that is seen when the program is first started
		open_screen_dialog = new JDialog(frame, "Game Designer Startup", true);
		open_screen_dialog.setLayout(new GridLayout(3,1));
		
		o_screen_new = new JButton("Create a New Galaxy");
		o_screen_new.setMnemonic(KeyEvent.VK_N);
		o_screen_new.addActionListener(this);
		open_screen_dialog.add(o_screen_new);
		
		o_screen_load = new JButton("Open an Existing Galaxy");
		o_screen_load.setMnemonic(KeyEvent.VK_O);
		o_screen_load.addActionListener(this);
		open_screen_dialog.add(o_screen_load);
		
		o_screen_exit = new JButton("Exit");
		o_screen_exit.setMnemonic(KeyEvent.VK_X);
		o_screen_exit.addActionListener(this);
		open_screen_dialog.add(o_screen_exit);
		
		open_screen_dialog.pack();
		showOpenScreenDialog();
	}
	
	public void actionPerformed(ActionEvent e)
	{		
		if(e.getSource()==new_item || e.getSource()==o_screen_new)
			newFile();
		else if(e.getSource()==open_item || e.getSource()==o_screen_load)
		{
			closeOpenScreenDialog();
			if((map instanceof Galaxy && confirmClose())||!(map instanceof Galaxy))
				load();
		}
		else if (e.getSource()==close_item)
			close();
		else if (e.getSource()==save_item)
			save(false);
		else if (e.getSource()==saveas_item)
			save(true);
		else if (e.getSource()==exit_item || e.getSource()==o_screen_exit)
			exitProgram();
		else if (e.getSource() == name_gal_item)
			nameGalaxy();
		else if (e.getSource() == rand_name_sys_item)
			randomizeSystemNames();
		else if (e.getSource() == rand_name_satellites_item)
			randomizeSatelliteNames();
		else if (e.getSource() == set_start_loc_item)
			;//BOOKMARK!  Must be fixed!
		else if (e.getSource()==t_add)
			addSystemOnClick();
		else if (e.getSource()==c_add)
			addSystem(c_x,c_y);
		else if (e.getSource()==c_edit || e.getSource()==t_edit)
		{
			//note that this can only happen when selected_systems has only 1 element
			for(GSystem sys : selected_systems)
				new SystemViewer(frame, sys);
			setUndoPoint();
		}
		else if (e.getSource()==c_delete || e.getSource()==t_delete)
			deleteSystem();
		else if (e.getSource()==c_range || e.getSource()==t_range)
		{
			if(drag_options != DRAG_RANGE)
			{
				show_dist.setEnabled(true);
				drag_options=DRAG_RANGE;
				displayLimit();
				drawGalaxy();
			}
			else
			{
				show_dist.setEnabled(false);
				drag_options=DRAG_NONE;
				drawGalaxy();
			}
		}
		else if (e.getSource()==c_distance || e.getSource()==t_distance)
		{
			if(drag_options != DRAG_DIST)
			{
				show_dist.setEnabled(true);
				drag_options=DRAG_DIST;
				displayLimit();
				drawGalaxy();
			}
			else
			{
				show_dist.setEnabled(false);
				drag_options=DRAG_NONE;
				drawGalaxy();
			}
		}
		else if (e.getSource()==t_nav || e.getSource()==c_nav)
			setNavigability();
		else if(e.getSource()==t_disp_unnav_sys)
		{
			display_unnavigable=t_disp_unnav_sys.isSelected();//checkbox
			drawGalaxy();
		}
		else if(e.getSource()==t_disp_navs)
		{
			nav_display = t_disp_navs.getSelectedIndex();//combo box
			drawGalaxy();
		}
		else if (e.getSource()==help_item)
			help();
		else if (e.getSource()==about_item)
		{
			n.showMessage("Made by David Goldstein and Yongzuan \"Mike\" Wu");
		}
	}
	
	private void showOpenScreenDialog()
	{
		open_screen_dialog.setLocation(new Point((frame.getWidth()-open_screen_dialog.getWidth())/2, (frame.getHeight()-open_screen_dialog.getHeight())/2));
		open_screen_dialog.setVisible(true);
	}
	
	private void closeOpenScreenDialog()
	{
		open_screen_dialog.setVisible(false);
	}
	
	private void newFile()
	{
		if((map instanceof Galaxy && confirmClose())||!(map instanceof Galaxy))
		{
			map=new Galaxy();
			drawGalaxy();
			fileIsOpen();
		}
	}
	
	private void fileIsOpen()
	{
		noSystemSelected();
		//enable file options
		close_item.setEnabled(true);
		save_item.setEnabled(true);
		saveas_item.setEnabled(true);
		
		//enable viewing options
		t_nav.setEnabled(true);
		t_disp_navs.setEnabled(true);
		t_disp_unnav_sys.setEnabled(true);
		
		closeOpenScreenDialog();
		
		//create the UndoRedoStack
		Object[] o = {map, selected_systems};
		undostack = new UndoRedoStack(o);
	}
	
	private void fileIsNotOpen()
	{
		//disable toolbar
		t_add.setEnabled(false);
		t_edit.setEnabled(false);
		t_range.setEnabled(false);
		t_delete.setEnabled(false);
		t_distance.setEnabled(false);
		show_dist.setEnabled(false);
		t_nav.setEnabled(false);
		n_slide.setEnabled(false);
		
		//disable context menu options
		c_add.setEnabled(false);
		c_edit.setEnabled(false);
		c_range.setEnabled(false);
		c_delete.setEnabled(false);
		c_distance.setEnabled(false);
		c_nav.setEnabled(false);
		
		//disable specific file options
		close_item.setEnabled(false);
		save_item.setEnabled(false);
		saveas_item.setEnabled(false);
		
		//disable viewing options
		t_disp_navs.setEnabled(false);
		t_disp_unnav_sys.setEnabled(false);
		
		undostack=null;
	}
	
	private void noSystemSelected()
	{
		//enable only add; disable rest of toolbar
		t_add.setEnabled(true);
		t_edit.setEnabled(false);
		t_range.setEnabled(false);
		t_delete.setEnabled(false);
		t_distance.setEnabled(false);
		show_dist.setEnabled(false);
		n_slide.setEnabled(true);
		
		//hmm... leave this or not?
		drag_options=DRAG_NONE;
		
		c_add.setEnabled(true);
		c_edit.setEnabled(false);
		c_range.setEnabled(false);
		c_delete.setEnabled(false);
		c_distance.setEnabled(false);
		c_nav.setEnabled(false);
	}
	
	private void systemIsSelected()
	{
		if(selected_systems.size()==1)
		{
			t_edit.setEnabled(true);
			c_edit.setEnabled(true);
			for(GSystem sys : selected_systems)
				t_nav.setValue((Object)sys.navigability);
		}
		else
		{
			t_edit.setEnabled(false);
			c_edit.setEnabled(false);
		}
		
		
		t_add.setEnabled(true);
		t_range.setEnabled(true);
		t_delete.setEnabled(true);
		t_distance.setEnabled(true);
		if(drag_options==DRAG_NONE)
			show_dist.setEnabled(false);
		else
			show_dist.setEnabled(true);
		n_slide.setEnabled(true);
		
		c_add.setEnabled(false);
		c_range.setEnabled(true);
		c_delete.setEnabled(true);
		c_distance.setEnabled(true);
		c_nav.setEnabled(true);
	}
	
	//file specific functions
	private void save(boolean save_as)
	{
		if(map instanceof Galaxy)
		{
			if(!(map.name instanceof String) || map.name =="")
			{
				String temp_name="";

				do{
					temp_name = JOptionPane.showInputDialog(frame, "The map does not have a name yet.  What would you like to name it?", "Please name the map", JOptionPane.QUESTION_MESSAGE);
					if(!(temp_name instanceof String)){
						JOptionPane.showMessageDialog(frame, "Saving cancelled.  The map must be given a valid name to be able to be saved.", "Nota Bene", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}while(temp_name.matches("\\s+") || temp_name =="");
				
				map.name = temp_name;
			}
			

			
			boolean ask=(save_as||!(cur_file instanceof File));
			
			int returnVal=0;
			if(ask)
				returnVal = fc.showSaveDialog(frame);
			
			if (returnVal == JFileChooser.APPROVE_OPTION || !ask)
				{
				if(ask)
					cur_file = fc.getSelectedFile();
			
				try
				{
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(cur_file));
					XMLEncoder2 e = new XMLEncoder2(os);
	
					e.writeObject(map);
					e.finish();
					try
					{
						os.close();
					}
					catch(IOException ioe){}
				}
				catch(FileNotFoundException f)
				{
					System.err.println("File not found exception in function save");
				}
			}
		}
	}

	private void load()
	{
		int returnVal = fc.showOpenDialog(frame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			cur_file = fc.getSelectedFile();
			try
			{
				XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(cur_file)));
				map = (Galaxy)d.readObject();
				d.close();
				fileIsOpen();
				drawGalaxy();
			}
			catch(FileNotFoundException e)
			{
				System.err.println("File not found exception in function load");
			}
		}
		else
			showOpenScreenDialog();
	}
	
	private void close()
	{
		if(map instanceof Galaxy)
		{
			if(confirmClose())
			{
				map=null;
				cur_file=null;
				panel.paintGalaxy(null,null, DRAG_NONE, GalacticStrategyConstants.DEFAULT_NAV_LEVEL, GalacticStrategyConstants.DEFAULT_NAV_OPTIONS, display_unnavigable);
				
				fileIsNotOpen();
				showOpenScreenDialog();
			}
		}
	}
	
	private boolean confirmClose()
	{
		return (JOptionPane.showConfirmDialog(frame, "Are you sure that you want to close the current map?\nAll unsaved data will be lost.", "Confirm Close", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION);
	}
	
	public void exitProgram()
	{
		if(map instanceof Galaxy)
		{
			int val = JOptionPane.showConfirmDialog(frame, "Do you want to save your file before closing the program?\nAll unsaved data will be lost.", "Exit Without Save?", JOptionPane.YES_NO_CANCEL_OPTION);
			switch(val)
			{
				case JOptionPane.YES_OPTION:
					save(false);
					break;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
					return;
			}
		}
		
		frame.dispose();
	}
	
	public void nameGalaxy()
	{
		String temp_name="";

		do{
			if(!(map.name instanceof String))
				temp_name = JOptionPane.showInputDialog(frame, "The map does not have a name yet.  What would you like to name it?", "Please name the map", JOptionPane.QUESTION_MESSAGE);
			else
				temp_name = JOptionPane.showInputDialog(frame, "The map's name is: " + map.name + "\nType a new name below and click OK, or\n click CANCEL to keep the current name", map.name);
			if(!(temp_name instanceof String))
				return; //the user clicked cancel
		}while(temp_name.matches("\\s+") || temp_name =="");
		
		map.name=temp_name;
	}
	
	public void keyPressed(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	public void keyReleased(KeyEvent e)
	{
		//System.out.println("KEY!!!");
		if(undostack.undoPossible() && UndoRedoStack.isCtrlZ(e))
		{
			//System.out.println("undo");
			Object[] o = undostack.undoLoad();
			map = (Galaxy)o[0];
			selected_systems = (HashSet<GSystem>)o[1]; //THIS GENERATES A WARNING - unchecked cast
			drawGalaxy();
		}
		else if(undostack.redoPossible() && UndoRedoStack.isCtrlY(e))
		{
			//System.out.println("redo");
			Object[] o = undostack.redoLoad();
			map = (Galaxy)o[0];
			selected_systems = (HashSet<GSystem>)o[1]; //THIS GENERATES A WARNING - unchecked cast
			drawGalaxy();
		}
	}
	
	private void randomizeSystemNames()
	{		
		LinkedHashSet<String> name_choices = new LinkedHashSet<String>(225); //set the initial capacity of the hashset, so it doesn't waste time expanding
		
		try{
			BufferedReader r = new BufferedReader(new FileReader("star names.txt"));
			
			String temp;
			boolean end=false;
			while(!end)
			{
				temp = r.readLine();
				if(temp instanceof String)
					name_choices.add(temp);
				else
					end=true;
			}
		} catch(FileNotFoundException fnfe) {
			System.out.println("star names.txt not found!");
			return;
		} catch(IOException ioe) {
			System.out.println("IO error loading star names.");
			return;
		}
		
		for(GSystem sys : map.systems){
			if(sys.name instanceof String)
				name_choices.remove(sys.name);
		}
		
		int i, choice;
		Random generator= new Random(System.nanoTime());
		for(GSystem sys : map.systems){
			if(!(sys.name instanceof String))
			{
				//pick random name from name_choices
				choice = generator.nextInt(name_choices.size());
				i=0;
				for(String nm : name_choices)
				{
					if(i == choice)
						sys.name = nm;
					i++;
				}
				name_choices.remove(sys.name);
			}
		}
		
		drawGalaxy();
	}
	
	private void randomizeSatelliteNames()
	{		
		LinkedHashSet<String> name_choices = new LinkedHashSet<String>(13805); //set the initial capacity of the hashset, so it doesn't waste time expanding
		
		try{
			BufferedReader r = new BufferedReader(new FileReader("planet names.txt"));
			
			String temp;
			boolean end=false;
			while(!end)
			{
				temp = r.readLine();
				if(temp instanceof String)
					name_choices.add(temp);
				else
					end=true;
			}
		} catch(FileNotFoundException fnfe) {
			System.out.println("planet names.txt not found!");
			return;
		} catch(IOException ioe) {
			System.out.println("IO error loading planet names.");
			return;
		}
		
		for(GSystem sys : map.systems){
			if(sys.name instanceof String)
				name_choices.remove(sys.name);
		}
		
		int i, choice;
		Random generator= new Random(System.nanoTime());
		for(GSystem sys : map.systems){
			for(Satellite sat : sys.orbiting_objects)
			{
				if(sat.name.length() == 0)
				{
					//pick random name from name_choices
					choice = generator.nextInt(name_choices.size());
					i=0;
					for(String nm : name_choices)
					{
						if(i == choice)
							sat.name = nm;
						i++;
					}
					name_choices.remove(sat.name);
				}
				
				if(sat instanceof Planet)
				{
					for(Satellite sat2 : ((Planet)sat).satellites)
					{
						if(sat2.name.length() == 0)
						{
							//pick random name from name_choices
							choice = generator.nextInt(name_choices.size());
							i=0;
							for(String nm : name_choices)
							{
								if(i == choice)
									sat2.name = nm;
								i++;
							}
							name_choices.remove(sat2.name);
						}
					}
				}
			}
		}
	}
	
	public void mousePressed(MouseEvent e)
	{
		try
		{
			GSystem sys=locateSystem(e.getX(),e.getY());
			
			if(selected_systems instanceof HashSet)//prepare for potential drag
			{
				boolean match=false;
				//multiple system dragging set up
				drag_box_right=0;
				drag_box_left=0;
				drag_box_top=0;
				drag_box_bottom=0;
				
				for(GSystem system : selected_systems)
				{
					if(system==sys)
						match=true;
					
					//set x_agj and y_adj so when you drag multiple systems the relative positions of those systems don't change
					system.x_adj=system.x-e.getX();
					system.y_adj=system.y-e.getY();
					
					//if statements to determine "drag_box" by saving the greatest x and y adj's
					if(system.x_adj > drag_box_right)
						drag_box_right=system.x_adj;
					else if(system.x_adj < drag_box_left)
						drag_box_left=system.x_adj;
					
					if(system.y_adj > drag_box_bottom)
						drag_box_bottom=system.y_adj;
					else if(system.y_adj < drag_box_top)
						drag_box_top=system.y_adj;
				}
				//user must press the mouse on one of the selected systems to drag all of them.  this behavior is controlled with drag_start
				drag_start=match;
			}
		}
		catch(NoSystemLocatedException x)
		{
			if(!e.isShiftDown() && !e.isAltDown())
			{
				selected_systems=null;
				if(map instanceof Galaxy)
					noSystemSelected();
			}
			drag_start=false; //insures that the select box behavior is viable
		}
		c_x=e.getX();
		c_y=e.getY();
		shift_down_on_click = e.isShiftDown();
		alt_down_on_click=e.isAltDown() && !shift_down_on_click; //shift takes precedence to alt
		select_but=e.getButton();
	}
	
	public void mouseReleased(MouseEvent e)
	{
		c_x=e.getX();
		c_y=e.getY();
		
		//don't unselect systems after dragging
		if(!drag_end)
		{
			try
			{
				GSystem temp_sys = locateSystem(e.getX(),e.getY());
				selectSystem(temp_sys, e.getButton());
			}
			catch(NoSystemLocatedException x)
			{
				if(!shift_down_on_click && !alt_down_on_click){
					selected_systems=null;
					drawGalaxy();
					
					if(map instanceof Galaxy)
						noSystemSelected();
					else
						fileIsNotOpen();
				}
			}
		
			maybeShowContextMenu(e);
			if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2 && selected_systems instanceof HashSet) //ignore double right click, and double left click when no systems are selected
			{
				if(selected_systems.size()==1) //double click only opens a system when only 1 system is selected.  Happens when alt/shift not held
				{
					//open system
					for(GSystem sys : selected_systems)
						new SystemViewer(frame, sys);
					setUndoPoint();
				}
			}
		}
		else //this is the end of a mouse drag
		{
			drag_end=false;
			drawGalaxy();
			if(selected_systems instanceof HashSet && selected_systems.size()!=0)
				setUndoPoint(); //the drag is captured and undoable
		}
		
		if(wait_to_add_sys)
		{
			addSystem(e.getX(), e.getY());
			wait_to_add_sys=false;
		}
		
		if(possibly_sel_desel_sys instanceof HashSet)
		{
			selected_systems = combineSelection();
			possibly_sel_desel_sys=null;
			if(selected_systems.size()==0){
				selected_systems = null;
				noSystemSelected();
			} else
				systemIsSelected();
			
			drawGalaxy();
		}
		
		alt_down_on_click=false;
		shift_down_on_click=false;
	}
	
	private void setUndoPoint()
	{
		Object[] o = {map, selected_systems};
		undostack.objectsChanged(o);
	}
	
	private boolean maybeShowContextMenu(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			context_menu.show(e.getComponent(),e.getX(), e.getY());
			return true;
		}
		else
			return false;
	}
	
	//start drag-and-move system code
	public void mouseDragged(MouseEvent e)
	{		
		if(!wait_to_add_sys && map instanceof Galaxy)
		{
			if(selected_systems instanceof HashSet && drag_start)
			{
				boolean modified=false;
				//checks to make sure that no systems in a multi-system drag a moved outside the map area
				if(e.getX()+drag_box_left > 0 && e.getX()+drag_box_right < 800) //BOOKMARK
				{
					for(GSystem sys : selected_systems)
						sys.x=e.getX()+sys.x_adj;
					modified=true;
				}
				if(e.getY()+drag_box_top > 0 && e.getY()+drag_box_bottom < 600) //BOOKMARK
				{
					for(GSystem sys : selected_systems)
						sys.y=e.getY()+sys.y_adj;
					modified=true;
				}
				
				if (modified)
					drawGalaxy();
				
				drag_end=true;
			}
			else if(select_but==MouseEvent.BUTTON1)
			{
				//selects multiple systems
				int x1;
				int y1;
				int x2;
				int y2;
				
				if(c_x<e.getX())
				{
					x1=c_x;
					x2=e.getX();
				}
				else
				{
					x1=e.getX();
					x2=c_x;
				}
				
				if(c_y<e.getY())
				{
					y1=c_y;
					y2=e.getY();
				}
				else
				{
					y1=e.getY();
					y2=c_y;
				}
				
				possibly_sel_desel_sys=new HashSet<GSystem>();
				
				if(map.systems instanceof HashSet)
				{
					for(GSystem sys : map.systems)
					{
						//if system is with the selected area and navigable
						if(x1 <= sys.x && sys.x <= x2 && y1 <= sys.y && sys.y < y2 && (sys.navigability >= current_nav_level || display_unnavigable))
							possibly_sel_desel_sys.add(sys); //duplicates automatically prevented
					}
				}
				
				panel.paintSelect(map, combineSelection(), drag_options, current_nav_level, nav_display, display_unnavigable, x1, y1, x2, y2);
				drag_end=true;
				
				if(selected_systems instanceof HashSet && selected_systems.size() != 0)
					systemIsSelected();
				else
					selected_systems=null;
			}
		} 
	}
	//end drag-and-move system code
	
	private HashSet<GSystem> combineSelection()
	{
		HashSet<GSystem> draw_as_sel_sys=new HashSet<GSystem>();
		if((shift_down_on_click || alt_down_on_click) && selected_systems instanceof HashSet)
			draw_as_sel_sys.addAll(selected_systems);
		
		if(alt_down_on_click)
			draw_as_sel_sys.removeAll(possibly_sel_desel_sys);
		else
			draw_as_sel_sys.addAll(possibly_sel_desel_sys);
		
		return draw_as_sel_sys;
	}
	
	public void mouseMoved(MouseEvent e)
	{
		if(wait_to_add_sys)
			panel.paintGhostSystem(map, selected_systems, drag_options, current_nav_level, nav_display, display_unnavigable, e.getX(), e.getY());
	}
		
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseClicked(MouseEvent e)
	{
		panel.requestFocusInWindow(); //this is necessary because otherwise toolbar elements can steal and hog the focus
	}
	
	private GSystem locateSystem(int x_pos, int y_pos) throws NoSystemLocatedException
	{
		if(map instanceof Galaxy && map.systems instanceof HashSet)
		{
			for(GSystem sys : map.systems)
			{
				if ((sys.navigability >= current_nav_level || display_unnavigable) && sys.x-5 <= x_pos && x_pos <= sys.x+5 && sys.y-5 <= y_pos && y_pos <= sys.y+5)
				{
					//an applicable system has been found near the click.
					return sys;
				}
			}
		}
		
		throw new NoSystemLocatedException(x_pos,y_pos);
	}
	
	private void selectSystem(GSystem sys, int button)
	{
		//shift will allow the use to select multiple systems. shift-clicking a selected system deselects it.
		if(button==MouseEvent.BUTTON1){
			if((shift_down_on_click||alt_down_on_click) && selected_systems instanceof HashSet)
			{
				if(!selected_systems.contains(sys))
					selected_systems.add(sys);
				else if(selected_systems.contains(sys) && (button==MouseEvent.BUTTON1 || alt_down_on_click))
				{
					selected_systems.remove(sys);
					
					//make sure that we didn't wipe out the selected_system list
					if(selected_systems.size() ==0)
					{			
						//if there are no novigable systems left, do this:
						
						selected_systems=null;
						noSystemSelected();
					} else {
						systemIsSelected();
					}
				}
				drawGalaxy();
				return;
			} else {
				selected_systems = new HashSet<GSystem>();
				selected_systems.add(sys);
			}
		}
		else //right click, eliminate the need to hold the shift key when right clicking to pull up the context menu with multiple systems
		{
			//determine if system is selected.  if not, it will become the only selected system.  if it is, do nothing here.
			boolean match=false;
			if(selected_systems instanceof HashSet)
				match=selected_systems.contains(sys);
				
			if(!match)
			{
				selected_systems=new HashSet<GSystem>();
				selected_systems.add(sys);
			}
		}
		
		drawGalaxy();
		systemIsSelected();
	}
	
	private void drawGalaxy()
	{
		panel.paintGalaxy(map, selected_systems, drag_options, current_nav_level, nav_display, display_unnavigable);
	}
	
	private class NoSystemLocatedException extends Exception
	{
		int x;
		int y;
		
		private NoSystemLocatedException(int x, int y)
		{
			super("NoSystemLocatedException from point ("+Integer.toString(x)+","+Integer.toString(y)+").");
			
			this.x=x;
			this.y=y;
		}
	}
	
	private void addSystem(int x, int y)
	{
		GSystem new_sys = new GSystem(x,y,null,new HashSet<Satellite>(),null,Integer.parseInt(t_nav.getValue().toString()));
		if(!(map.systems instanceof HashSet))
			map.systems = new HashSet<GSystem>();
		map.systems.add(new_sys);
		
		selected_systems = new HashSet<GSystem>();
		selected_systems.add(new_sys);
		drawGalaxy();
		
		systemIsSelected();
		setUndoPoint();
	}
	
	private void addSystemOnClick()
	{
		wait_to_add_sys=true;
	}
	
	private void deleteSystem()
	{
		map.systems.removeAll(selected_systems);
		selected_systems=null;
		drawGalaxy();
		
		noSystemSelected();
		setUndoPoint();
	}
	
	Action delete_action = new AbstractAction()
	{
		public void actionPerformed(ActionEvent e)
		{
			if(selected_systems instanceof HashSet)
				deleteSystem();
		}
	};
	
	private void setNavigability()
	{
		//EDIT HERE FOR VARIABLE MAX AND MIN **************************BOOKMARK
		String[] choices={"10","9","8","7","6","5","4","3","2","1"};
		String val=(String)JOptionPane.showInputDialog(frame,"Select the desired navigability level from the list.\nKeep in mind that 10 is easiest to navigate to and 1 is the hardest.", "Set Navigability", JOptionPane.QUESTION_MESSAGE, null, choices,"10");
		if(val instanceof String)
		{
			int nav=Integer.valueOf(val);
			
			for(GSystem sys : selected_systems)
			{
				sys.navigability=nav;
			}
			
			if(nav < current_nav_level && !display_unnavigable)
			{
				selected_systems=null;
				noSystemSelected();
			}
			
			if(nav_display==NAV_DISP_NONE)
			{
				nav_display=NAV_DISP_SELECTED;
				t_disp_navs.setSelectedIndex(NAV_DISP_SELECTED);
			}
			
			setUndoPoint();
			drawGalaxy();
		}
	}
	
	//listener function for the navigability and distance sliders
	public void stateChanged(ChangeEvent e)
	{
		if(e.getSource()==show_dist)
		{
			panel.max_dist_shown=(int)((JSlider)e.getSource()).getValue();
			displayLimit();
			if(selected_systems instanceof HashSet)
				drawGalaxy();
		}
		else if(e.getSource()==n_slide)
		{
			current_nav_level=(int)((JSlider)e.getSource()).getValue();
			/*if(selected_systems instanceof HashSet && !display_unnavigable)
			{
				//scan navigabilities and create a list of still-navigable systems selected.
				// do not remove directly to avoid Concurrent Modification exception
				HashSet<GSystem> sys_to_remove=new HashSet<GSystem>();
				for(GSystem sys : selected_systems)
				{
					if (sys.navigability < current_nav_level)
						sys_to_remove.add(sys);
				}
				selected_systems.removeAll(sys_to_remove);
				
				if(selected_systems.size() == 0)
				{
					//if there are no novigable systems left (selected), do this:
					
					selected_systems=null;
					noSystemSelected();
				}
			}*/
			
			drawGalaxy();
		}
		else if(e.getSource()==t_nav)
		{
			int set_nav_to = Integer.valueOf(((JSpinner)e.getSource()).getValue().toString());
			if(selected_systems instanceof HashSet)
			{
				for(GSystem sys : selected_systems)
					sys.navigability = set_nav_to;
			}
			else if(set_nav_to < current_nav_level && !display_unnavigable)
			{
				selected_systems=null;
				noSystemSelected();
			}
			
			if(nav_display==NAV_DISP_NONE)
			{
				nav_display=NAV_DISP_SELECTED;
				t_disp_navs.setSelectedIndex(NAV_DISP_SELECTED);
			}
			drawGalaxy();
			setUndoPoint();
		}
	}
	
	private void displayLimit()
	{
		n.showMessage("Limit = "+ Integer.toString(panel.max_dist_shown));
	}
	
	private void help()
	{
		JDialog helper=new JDialog(frame, "Galaxy Designer Help", true);
			
		JTabbedPane help_tabs=new JTabbedPane(JTabbedPane.LEFT);
		
		//START HEIGHT FINDING
		//to start calculate height.  Use largest text - here, system management.  Exact copy of below except for the height setting htmk
		String sys_text="<html><body style='padding:2px 2px 2px 4px' width=\"400\"><p>"
			+ "To add a system to the galaxy, either click the 'Add' button "
			+ "on the toolbar or press ALT+A and then click where you want to place the system.  Alternatively, you can right "
			+ "click where you want to place a new system and press add on the context menu.  After systems have been added, "
			+ "they can be moved around, edited, or deleted.</p><br />"
			+ "<p>To select a single system, just click on it.  To select multiple systems at a time, either shift-click each desired system once or hold "
			+ "the SHIFT key and drag a box to select systems and hold ALT to drag a box deselecting selected systems.  Once "
			+ "a group of systems is selected, they can be dragged together, deleted, or set to the same navigability.</p><br />"
			+ "<p>To delete systems, select them and click the 'Delete' button on the toolbar, press ALT+D, or the delete key.</p><br />"
			+ "<p>To move systems, select them and then click and drag one of the selected systems.</p>";
		
		JLabel sys_desc=new JLabel(sys_text);
		JScrollPane sys_pane = new JScrollPane(sys_desc);
		help_tabs.addTab("Managing Systems", sys_pane);
		
		int the_height = (int)help_tabs.getPreferredSize().getHeight();
		
		help_tabs.remove(0);
		//END HEIGHT FINDING
		
		
		String overview_text="<html><body style='padding:2px 2px 2px 4px' width=\"400\" height=\""+the_height+"\"><p>"
			+ "The Galaxy Designer is a tool designed to create galaxies for the game Space Wars."
			+ "  The galaxy of Space Wars is organized in a few ways: first of all, everything is organized into systems."
			+ "  The galactic map displays systems positions relative to another."
			+ "  These systems consist of stars, planets, moons, space stations, etc."
			+ "</p><br /><p>"
			+ "Before you can start designing a galaxy, you must either create a new file with "
			+ "File->New (Ctrl+N) or open a file via File->Open (Ctrl+O).  Once files are opened, they can be saved"
			+ " with File->Save (Ctrl+S) and File->Save As.  Opened files can also be closed.  The program will ask for you to"
			+ " confirm actions that involve closing the current file, i.e., creating a new file, opening a file, closing the current"
			+ " file, and exiting the program."
			+ "</p></body></html>";
		JLabel overview_desc=new JLabel(overview_text);
		JScrollPane overview_pane=new JScrollPane(overview_desc);
		help_tabs.addTab("Overview", overview_pane);
		
		//the text we used to find the height
		sys_text="<html><body style='padding:2px 2px 2px 4px' width=\"400\" height=\""+the_height+"\"><p>"
			+ "To add a system to the galaxy, either click the 'Add' button "
			+ "on the toolbar or press ALT+A and then click where you want to place the system.  Alternatively, you can right "
			+ "click where you want to place a new system and press add on the context menu.  After systems have been added, "
			+ "they can be moved around, edited, or deleted.</p><br />"
			+ "<p>To select a single system, just click on it.  To select multiple systems at a time, either shift-click each desired system once or hold "
			+ "the SHIFT key and drag a box to select systems and hold ALT to drag a box deselecting selected systems.  Once "
			+ "a group of systems is selected, they can be dragged together, deleted, or set to the same navigability.</p><br />"
			+ "<p>To delete systems, select them and click the 'Delete' button on the toolbar, press ALT+D, or the delete key.</p><br />"
			+ "<p>To move systems, select them and then click and drag one of the selected systems.</p>";
		sys_desc=new JLabel(sys_text);
		sys_pane = new JScrollPane(sys_desc);
		help_tabs.addTab("Managing Systems", sys_pane);
		
		String nav_text="<html><body style='padding:2px 2px 2px 4px' width=\"400\" height=\""+the_height+"\"><p>"
			+ "Navigability is the ease with which a system can be reached.  Navigability is represented by an integer from"
			+ " 1 to 10.  Systems with a navigability of 10 are extremely easy to reach, while systems with lower navigabilities"
			+ " will require better astronomical data for a player to reach.  The Navigability slider on the toolbar changes the lowest"
			+ " navigability shown on the galactic map.  For instance, if the slider is set at 5, you can see any system with a"
			+ " navigability of 5 or greater.  Effectively, sliding the slider to 1 at the far left will display all systems."
			+ "</p><br /><p>"
			+ "When adding systems, the \"set navigability\" box sets the navigability they are automatically given.  You "
			+ "can use the \"set navigability\" feature to change the navigability of any individual system."
			+ "</p></body></html>";
		JLabel nav_desc=new JLabel(nav_text);
		JScrollPane nav_pane=new JScrollPane(nav_desc);
		help_tabs.addTab("Navigability", nav_pane);
		
		help_tabs.setSelectedIndex(0);
		helper.add(help_tabs);
		//help_tabs.setPreferredSize(new Dimension((int)help_tabs.getPreferredSize().getWidth()+20,225));
		
		helper.pack();
		helper.setVisible(true);
	}

	//window listener used to check if you want to save your file when frame's X is clicked
	public void windowClosing(WindowEvent e)
	{
		exitProgram();
	}
	
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}