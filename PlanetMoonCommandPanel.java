import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class PlanetMoonCommandPanel extends JPanel implements ActionListener
{
	Satellite<?> the_sat;
	JButton build;
	JButton cancel;
	JPanel stats_panel;
		JLabel pop;
		JLabel building_limit;
	
	JPanel facilities_panel;
	GroupLayout.ParallelGroup vgroup;
	GroupLayout.SequentialGroup hgroup;
	
	Shipyard the_shipyard;
	JButton build_ship;
	JButton cancel_build_ship; //this button allows the user to go from the selection of a ship to build back to the view of the queue without building a ship or unselecting the planet/moon.  It also serves to go from the view of the shipyard to the view of the facilities
	
	//for facility-building progress
	JProgressBar progress_bar;
	boolean need_to_reset;
	JPanel cur_fac_in_prog_panel;
	
	boolean no_base_mode;
	
	int state;
		final static int FACILITIES_DISPLAYED = 0;
		final static int SHIP_QUEUE_DISPLAYED = 1;
		final static int SHIP_CHOICES_DISPLAYED = 2;
		final static int FACILITY_CHOICES_DISPLAYED = 3;
	
	
	HashSet<FacilityStatusUpdater> facility_updaters;
	
	public PlanetMoonCommandPanel()
	{
		super();
		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(layout);
		
		build=new JButton("Build Facility...");
		build.addActionListener(this);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		
		cur_fac_in_prog_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		cur_fac_in_prog_panel.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
		
		need_to_reset=false;
		
		facility_updaters=new HashSet<FacilityStatusUpdater>();
		
		facilities_panel = new JPanel();
		GroupLayout fac_panel_layout = new GroupLayout(facilities_panel);
		facilities_panel.setLayout(fac_panel_layout);
		facilities_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		
		vgroup = fac_panel_layout.createParallelGroup();
		hgroup = fac_panel_layout.createSequentialGroup();
		
		fac_panel_layout.setHorizontalGroup(hgroup);
		fac_panel_layout.setVerticalGroup(vgroup);
		
		stats_panel=new JPanel();
		BoxLayout stats_layout = new BoxLayout(stats_panel, BoxLayout.Y_AXIS);
		stats_panel.setLayout(stats_layout);
		
		no_base_mode = false;
	}
	
	public void setSat(Satellite<?> s)
	{
		the_sat=s;
		state = FACILITIES_DISPLAYED;
		
		//now update the panel
		removeAll();
		stats_panel.removeAll();
		
		facility_updaters.clear();
		facilities_panel.removeAll();
		facilities_panel.repaint();
		
		JPanel pic_panel = new JPanel();
		BoxLayout pic_layout = new BoxLayout(pic_panel, BoxLayout.Y_AXIS);
		pic_panel.setLayout(pic_layout);
		add(pic_panel);
		
		JLabel name_label = new JLabel(s.getName());
		JPanel name_panel = new JPanel();
		name_panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		name_panel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		name_panel.add(name_label);
		pic_panel.add(name_panel);
		
		ImageIcon pic = new ImageIcon(s.imageLoc());
		JLabel icon_label = new JLabel(pic);
		icon_label.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		pic_panel.add(icon_label);
		
		if(s instanceof OwnableSatellite<?>)
		{			
			//cancel any half-started build.
			cur_fac_in_prog_panel.removeAll();
			
			addStats();
			
			if(((OwnableSatellite<?>)s).getOwner() != null)
			{
				//color if there is an owner
				name_panel.setBackground(((OwnableSatellite<?>)s).getOwner().getColor());
				
				//if you are the owner, commands!
				if(((OwnableSatellite<?>)s).getOwner().getId() == GameInterface.GC.player_id)
				{					
					setUpFacilityBuilding();
				}
			}
			
			displayAllFacilities();
		}
		
		add(stats_panel);
		add(facilities_panel);
	}
	
	private void addStats()
	{
		pop = new JLabel("Population: " + ((OwnableSatellite<?>)the_sat).getPopulation());
		pop.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		stats_panel.add(pop);
		
		building_limit = new JLabel("Max Buildings: " + ((OwnableSatellite<?>)the_sat).getBuilding_limit());
		building_limit.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		stats_panel.add(building_limit);
	}
	
	private void setUpFacilityBuilding()
	{
		progress_bar = new JProgressBar(0,1000);
		stats_panel.add(progress_bar);
		
		//set the buttons to reflect building/not building
		FacilityType bldg_in_prog = ((OwnableSatellite<?>)the_sat).getBldg_in_progress();
		boolean is_building = (bldg_in_prog != FacilityType.NO_BLDG);
		build.setEnabled(!is_building);
		cancel.setEnabled(is_building);
		need_to_reset=is_building;
		
		JPanel button_strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		button_strip.add(build);
		button_strip.add(cancel);
		stats_panel.add(button_strip);
		
		if(is_building)
		{
			cur_fac_in_prog_panel.add(new JLabel(bldg_in_prog.icon));
			cur_fac_in_prog_panel.add(new JLabel(bldg_in_prog.name));
			stats_panel.add(cur_fac_in_prog_panel);
		}
	}
	
	public void update(long t)
	{
		if(the_sat instanceof OwnableSatellite<?>)
		{
			//update population reading
			pop.setText("Population: " + ((OwnableSatellite<?>)the_sat).getPopulation());
			building_limit.setText("Max Buildings: " + ((OwnableSatellite<?>)the_sat).getBuilding_limit());
			
			if(((OwnableSatellite<?>) the_sat).owner == GameInterface.GC.players[GameInterface.GC.player_id])
			{
				//if building in progress, update progress
				if(((OwnableSatellite<?>)the_sat).getBldg_in_progress() != FacilityType.NO_BLDG)
				{
					double prog = ((OwnableSatellite<?>)the_sat).constructionProgress(t);
					//System.out.println("PMCPanel update to progress " + Double.toString(prog));
					progress_bar.setValue((int)(1000.0*prog));
				}
				else if(need_to_reset)
				{
					progress_bar.setValue(0);
					cur_fac_in_prog_panel.removeAll();
					build.setEnabled(true);
					cancel.setEnabled(false);
					need_to_reset = false;
				}
			}
			
			for(FacilityStatusUpdater updater : facility_updaters)
				updater.updateFacility();
			
			no_base_mode=(((OwnableSatellite<?>)the_sat).the_base == null);
		}
	}
	
	public void displayAllFacilities()
	{
		synchronized(((OwnableSatellite<?>)the_sat).facilities)
		{
			for(Integer i : new TreeSet<Integer>(((OwnableSatellite<?>)the_sat).facilities.keySet()))
				displayFacility(((OwnableSatellite<?>)the_sat).facilities.get(i));
		}
	}
	
	public void displayFacility(Facility<?> f)
	{
		if(state == FACILITIES_DISPLAYED)
		{
			JPanel the_panel = new JPanel();
			BoxLayout bl = new BoxLayout(the_panel, BoxLayout.Y_AXIS);
			the_panel.setLayout(bl);
			the_panel.setMaximumSize(new Dimension(80, 140));
			the_panel.setBorder(BorderFactory.createLineBorder(Color.RED));
			
			the_panel.add(new JLabel(f.getName()));
			the_panel.add(new JLabel(new ImageIcon(f.imageLoc())));
			
			JProgressBar health_bar = new JProgressBar(0, f.getEndurance());
			health_bar.setMaximumSize(new Dimension(120,20));
			//health_bar.setPreferredSize(new Dimension(120,20));
			health_bar.setStringPainted(true);
			the_panel.add(health_bar);
			
			FacilityStatusUpdater updater;
			switch(f.getType())
			{
				case BASE:
					JLabel soldier_label = new JLabel("Soldiers: " + Integer.toString(((Base)f).getSoldierInt()));
					the_panel.add(soldier_label);
					//the_panel.add(new JLabel("Max Soldiers: " + Integer.toString(((Base)f).getMax_soldier())));
					updater = new BaseStatusUpdater(health_bar, soldier_label, (Base)f);
					break;
				case MINE:
					updater = new MineStatusUpdater(health_bar, (Mine)f);
					break;
				case TAXOFFICE:
					updater = new TaxOfficeStatusUpdater(health_bar, (TaxOffice)f);
					break;
				case SHIPYARD:
					//display objects in Queue and progress bar
					JProgressBar manufac_bar = new JProgressBar(0,100);
					manufac_bar.setMaximumSize(new Dimension(120, 20));
					manufac_bar.setStringPainted(true);
					the_panel.add(manufac_bar);
					updater = new ShipyardStatusUpdater(health_bar, manufac_bar, (Shipyard)f);
					if(((OwnableSatellite<?>)the_sat).owner != null && GameInterface.GC.player_id == ((OwnableSatellite<?>)the_sat).owner.getId())
						the_panel.addMouseListener(new ShipyardSelector((Shipyard)f, this));
					break;
				default:
					System.out.println("what sort of facility is " + f.getName() +"?");
					return;
			}
			facility_updaters.add(updater);
			updater.updateFacility();
			
			//facilities_panel.add(the_panel);
			hgroup.addComponent(the_panel);
			vgroup.addComponent(the_panel);
		}
	}
	
	public void shipyardDetails(Shipyard s)
	{
		the_shipyard = s;
		
		//clear facility_panel, and make space in the stats_panel.
		stats_panel.removeAll();
		addStats();
		stats_panel.add(progress_bar);
		
		state=SHIP_QUEUE_DISPLAYED;
		facility_updaters.clear();
		
		//set up the basic attributes section.  This includes the text "shipyard", picture, and health progress bar
		JPanel basic_attributes = new JPanel();
		GroupLayout attr_layout = new GroupLayout(basic_attributes);
		basic_attributes.setLayout(attr_layout);
		
		GroupLayout.ParallelGroup attr_hgroup = attr_layout.createParallelGroup();
		GroupLayout.SequentialGroup attr_vgroup = attr_layout.createSequentialGroup();
		
		attr_layout.setHorizontalGroup(attr_hgroup);
		attr_layout.setVerticalGroup(attr_vgroup);
		
		JLabel shipydtext = new JLabel("Shipyard");
		attr_hgroup.addComponent(shipydtext);
		attr_vgroup.addComponent(shipydtext);
		JLabel shipydimg = new JLabel(new ImageIcon("images/Shipyard.gif"));
		attr_hgroup.addComponent(shipydimg);
		attr_vgroup.addComponent(shipydimg);
		
		JProgressBar health_bar = new JProgressBar(0, s.getEndurance());
		health_bar.setMaximumSize(new Dimension(50,20));
		health_bar.setStringPainted(true);
		attr_hgroup.addComponent(health_bar);
		attr_vgroup.addComponent(health_bar);
		
		//set up the buttons
		JPanel button_panel = new JPanel(new GridLayout(3,1));
		
		build_ship = new JButton("Build...");
		build_ship.addActionListener(this);
		button_panel.add(build_ship);
		
		cancel_build_ship = new JButton("Back");
		cancel_build_ship.addActionListener(this);
		button_panel.add(cancel_build_ship);
		
		
		//put the attributs and buttons into one panel side by side
		JPanel attr_and_buttons = new JPanel();
		GroupLayout aab_layout = new GroupLayout(attr_and_buttons);
		attr_and_buttons.setLayout(aab_layout);
		
		GroupLayout.ParallelGroup aab_vgroup = aab_layout.createParallelGroup();
		GroupLayout.SequentialGroup aab_hgroup = aab_layout.createSequentialGroup();
		
		aab_layout.setHorizontalGroup(aab_hgroup);
		aab_layout.setVerticalGroup(aab_vgroup);
		
		aab_vgroup.addComponent(basic_attributes);
		aab_hgroup.addComponent(basic_attributes);
		aab_vgroup.addComponent(button_panel);
		aab_hgroup.addComponent(button_panel);
		
		stats_panel.add(attr_and_buttons);
		
		//show queue
		displayQueue();
		
		JProgressBar manufac_bar = new JProgressBar(0,100);
		manufac_bar.setStringPainted(true);
		stats_panel.add(manufac_bar);
		
		facility_updaters.add(new ShipyardStatusUpdater(health_bar, manufac_bar, s)); //just this one shipyard is updated
	}
	
	private void facilityChoices()
	{
		facilities_panel.removeAll();
		state = FACILITY_CHOICES_DISPLAYED;
		FacilityType[] fTypes = FacilityType.values();
		
		int first_fac;
		int end_facs;
		if(no_base_mode)
		{
			first_fac = 1;
			end_facs = 2;
		}
		else
		{
			first_fac = 2;
			end_facs = fTypes.length;
		}
		
		for(int i=first_fac; i < end_facs; i++) //start at 1 since NO_BUILDING is type 0
		{
			JPanel fac_panel = new JPanel();
			fac_panel.setToolTipText(fTypes[i].tooltip);
			fac_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
			fac_panel.setMinimumSize(new Dimension(65,130));
			fac_panel.addMouseListener(new ObjBuilder<FacilityType, OwnableSatellite<?>>((OwnableSatellite<?>)the_sat, fTypes[i], ObjBuilder.facilityManufactureFuncs, fac_panel, false, this));
			GroupLayout gl = new GroupLayout(fac_panel);
			fac_panel.setLayout(gl);
			
			GroupLayout.ParallelGroup glhgroup = gl.createParallelGroup();
			GroupLayout.SequentialGroup glvgroup = gl.createSequentialGroup();
			
			gl.setHorizontalGroup(glhgroup);
			gl.setVerticalGroup(glvgroup);
			
			JLabel name_label = new JLabel(fTypes[i].name);
			JLabel ship_pic = new JLabel(fTypes[i].icon);
			JLabel money_cost = new JLabel(Integer.toString(fTypes[i].money_cost) + " money");
			JLabel metal_cost = new JLabel(Integer.toString(fTypes[i].metal_cost) + " metal");
			
			glhgroup.addComponent(name_label);
			glvgroup.addComponent(name_label);
			
			glhgroup.addComponent(ship_pic);
			glvgroup.addComponent(ship_pic);
			
			glhgroup.addComponent(money_cost);
			glvgroup.addComponent(money_cost);
			
			glhgroup.addComponent(metal_cost);
			glvgroup.addComponent(metal_cost);
			
			hgroup.addComponent(fac_panel);
			vgroup.addComponent(fac_panel);
		}
	}
	
	public void displayQueue()
	{
		state = SHIP_QUEUE_DISPLAYED;
		facilities_panel.removeAll();
		synchronized(the_shipyard.queue_lock)
		{
			for(Integer i : the_shipyard.manufac_queue.keySet())
			{
				Ship s = the_shipyard.manufac_queue.get(i);
				JPanel ship_panel = new JPanel();
				GroupLayout gl = new GroupLayout(ship_panel);
				ship_panel.setLayout(gl);
				
				GroupLayout.ParallelGroup glhgroup = gl.createParallelGroup();
				GroupLayout.SequentialGroup glvgroup = gl.createSequentialGroup();
				
				gl.setHorizontalGroup(glhgroup);
				gl.setVerticalGroup(glvgroup);
				
				JPanel top_panel = new JPanel();
				JLabel name_label = new JLabel(s.type.name);
				JButton cancel_but = new JButton("Cancel");
				cancel_but.addActionListener(new QueueCanceller(this, the_shipyard, s));
				top_panel.add(name_label);
				top_panel.add(cancel_but);
				
				JLabel ship_pic = new JLabel(s.type.icon);
				
				glhgroup.addComponent(top_panel);
				glvgroup.addComponent(top_panel);
				
				glhgroup.addComponent(ship_pic);
				glvgroup.addComponent(ship_pic);
				
				hgroup.addComponent(ship_panel);
				vgroup.addComponent(ship_panel);
			}
		}
	}
	
	private void displayShipTypes()
	{
		facilities_panel.removeAll();
		state = SHIP_CHOICES_DISPLAYED;
		ShipType[] sTypes = ShipType.values();
		for(int i=1; i < sTypes.length; i++) //start at 1 since MISSILE is type 0
		{
			JPanel ship_panel = new JPanel();
			ship_panel.setToolTipText(sTypes[i].tooltip);
			ship_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
			ship_panel.addMouseListener(new ObjBuilder<ShipType, Shipyard>(the_shipyard, sTypes[i], ObjBuilder.shipManufactureFuncs, ship_panel, true, this));
			GroupLayout gl = new GroupLayout(ship_panel);
			ship_panel.setLayout(gl);
			
			GroupLayout.ParallelGroup glhgroup = gl.createParallelGroup();
			GroupLayout.SequentialGroup glvgroup = gl.createSequentialGroup();
			
			gl.setHorizontalGroup(glhgroup);
			gl.setVerticalGroup(glvgroup);
			
			JLabel name_label = new JLabel(sTypes[i].name);
			JLabel ship_pic = new JLabel(sTypes[i].icon);
			JLabel money_cost = new JLabel(Integer.toString(sTypes[i].money_cost) + " money");
			JLabel metal_cost = new JLabel(Integer.toString(sTypes[i].metal_cost) + " metal");
			
			glhgroup.addComponent(name_label);
			glvgroup.addComponent(name_label);
			
			glhgroup.addComponent(ship_pic);
			glvgroup.addComponent(ship_pic);
			
			glhgroup.addComponent(money_cost);
			glvgroup.addComponent(money_cost);
			
			glhgroup.addComponent(metal_cost);
			glvgroup.addComponent(metal_cost);
			
			hgroup.addComponent(ship_panel);
			vgroup.addComponent(ship_panel);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == build)
		{
			facilityChoices();
			build.setEnabled(false);
			cancel.setEnabled(true);
		}
		else if(e.getSource() == cancel)
		{
			if(state == FACILITY_CHOICES_DISPLAYED)
			{
				build.setEnabled(true);
				cancel.setEnabled(false);
				setSat(the_sat); //TODO: find better way to display facilities
			}
			else
			{
				GameInterface.GC.scheduleOrder(new CancelFacilityBuildOrder(((OwnableSatellite<?>)the_sat), GameInterface.GC.updater.getTime()));
			}
		}
		else if(e.getSource() == build_ship)
		{
			build_ship.setEnabled(false);
			displayShipTypes();
		}
		else if(e.getSource() == cancel_build_ship)
		{
			if(state == SHIP_CHOICES_DISPLAYED)
			{
				build_ship.setEnabled(true);
				displayQueue();
			}
			else
				setSat(the_sat); //TODO: find better way to display facilities
		}
	}
}