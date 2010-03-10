import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.HashSet;

public class PlanetMoonCommandPanel extends JPanel implements ActionListener, MouseListener
{
	Satellite the_sat;
	JButton build;
	JButton cancel;
	JLabel pop;
	JPanel choices_panel;
	JLabel cost_label;
	
	JPanel facilities_panel;
	GroupLayout.ParallelGroup vgroup;
	GroupLayout.SequentialGroup hgroup;
	
	JLabel base_icon;
	JLabel mine_icon;
	JLabel shipyard_icon;
	
	//for facility-building progress
	JProgressBar progress_bar;
	boolean need_to_reset;
	
	HashSet<FacilityStatusUpdater> facility_updaters;
	
	GameControl GC;
	
	public PlanetMoonCommandPanel(GameControl gc)
	{
		super();
		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(layout);
		
		//set up a pointer back to the main game data
		GC=gc;
		
		build=new JButton("Build Facility...");
		build.addActionListener(this);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		
		choices_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		choices_panel.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
		
		base_icon = new JLabel(new ImageIcon("images/Base.gif"));
		base_icon.addMouseListener(this);
		shipyard_icon = new JLabel(new ImageIcon("images/Shipyard.gif"));
		shipyard_icon.addMouseListener(this);
		mine_icon = new JLabel(new ImageIcon("images/Mine.gif"));
		mine_icon.addMouseListener(this);
		
		cost_label = new JLabel(" ");
		cost_label.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		
		progress_bar = new JProgressBar(0,1000);
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
	}
	
	public void setSat(Satellite s)
	{
		the_sat=s;

		//now update the panel
		removeAll();
		
		//cancel any half-started build.
		choices_panel.removeAll();
		//set the buttons to reflect building/not building
		boolean is_building = (((OwnableSatellite)the_sat).bldg_in_progress != Facility.NO_BLDG);
		build.setEnabled(!is_building);
		cancel.setEnabled(is_building);
		
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
		
		ImageIcon pic;
		if(s instanceof Planet)
			pic = new ImageIcon("images/planet.jpg");
		else
			pic = new ImageIcon("images/moon.jpg");
		JLabel icon_label = new JLabel(pic);
		icon_label.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		pic_panel.add(icon_label);
		
		JPanel stats_panel=new JPanel();
		BoxLayout stats_layout = new BoxLayout(stats_panel, BoxLayout.Y_AXIS);
		stats_panel.setLayout(stats_layout);
		add(stats_panel);
		
		add(facilities_panel);
		facilities_panel.removeAll();
		
		if(s instanceof OwnableSatellite)
		{
			pop = new JLabel("Population: " + ((OwnableSatellite)s).getPopulation());
			pop.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
			stats_panel.add(pop);
			
			if(((OwnableSatellite)s).getOwner() instanceof Player)
			{
				//color if there is an owner
				name_panel.setBackground(((OwnableSatellite)s).getOwner().getColor());
				
				//if you are the owner, commands!
				if(((OwnableSatellite)s).getOwner().getId() == GC.player_id)
				{
					stats_panel.add(progress_bar);
					
					//set the status of the buttons
					if(((OwnableSatellite)the_sat).bldg_in_progress == Facility.NO_BLDG)
					{
						build.setEnabled(true);
						cancel.setEnabled(false);
					}
					else
					{
						build.setEnabled(false);
						cancel.setEnabled(true);
					}
					
					JPanel button_strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
					button_strip.add(build);
					button_strip.add(cancel);
					stats_panel.add(button_strip);
					
					stats_panel.add(choices_panel);
					stats_panel.add(cost_label);
				}
				
				for(Facility f : ((OwnableSatellite)s).facilities)
					displayFacility(f);
			}
		}
	}
	
	public void update(long t)
	{
		if(the_sat instanceof OwnableSatellite)
		{
			//update population reading
			pop.setText("Population: " + ((OwnableSatellite)the_sat).getPopulation());
			
			//if building in progress, update progress
			if(((OwnableSatellite)the_sat).bldg_in_progress != Facility.NO_BLDG)
			{
				double prog = ((OwnableSatellite)the_sat).constructionProgress(t);
				progress_bar.setValue((int)(1000.0*prog));
			}
			else if(need_to_reset)
			{
				progress_bar.setValue(0);
				build.setEnabled(true);
				cancel.setEnabled(false);
				need_to_reset = false;
			}
			
			for(FacilityStatusUpdater updater : facility_updaters)
				updater.updateFacility();
		}
	}
	
	public void displayFacility(Facility f)
	{		
		JPanel the_panel = new JPanel();
		BoxLayout bl = new BoxLayout(the_panel, BoxLayout.Y_AXIS);
		the_panel.setLayout(bl);
		the_panel.setMaximumSize(new Dimension(80, 140));
		the_panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		the_panel.add(new JLabel(f.getName()));
		the_panel.add(new JLabel(new ImageIcon(f.getImgLoc())));
		
		JProgressBar health_bar = new JProgressBar(0, f.getEndurance());
		health_bar.setMaximumSize(new Dimension(120,20));
		//health_bar.setPreferredSize(new Dimension(120,20));
		health_bar.setStringPainted(true);
		the_panel.add(health_bar);
		
		FacilityStatusUpdater updater;
		switch(f.getType())
		{
			case Facility.BASE:
				JLabel soldier_label = new JLabel("Soldiers: " + Integer.toString(((Base)f).getSoldierInt()));
				the_panel.add(soldier_label);
				//the_panel.add(new JLabel("Max Soldiers: " + Integer.toString(((Base)f).getMax_soldier())));
				updater = new BaseStatusUpdater(health_bar, soldier_label, (Base)f);
				break;
			case Facility.MINE:
				updater = new MineStatusUpdater(health_bar, (Mine)f);
				break;
			case Facility.SHIPYARD:
				//display objects in Queue and progress bar
				JProgressBar manufac_bar = new JProgressBar(0,100);
				manufac_bar.setStringPainted(true);
				the_panel.add(manufac_bar);
				updater = new ShipyardStatusUpdater(health_bar, manufac_bar, (Shipyard)f);
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
	
	private void facilityChoices()
	{		
		choices_panel.add(base_icon);
		choices_panel.add(shipyard_icon);
		choices_panel.add(mine_icon);
	}
	
	private void executeBuild(int facility_id)
	{
		need_to_reset = ((OwnableSatellite)the_sat).scheduleConstruction(facility_id, GC.TC.getTime());
		//need_to_reset indicates if the construction was successfully started, and the interface will later need to be set back to its original state when the building is finished
		if(need_to_reset)
		{
			choices_panel.removeAll();
			cost_label.setText(" ");
		}
		else //not enough money/metal to build
			SoundManager.playSound("sound/doot doot.wav");//not enough resources - NOTIFY
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
			((OwnableSatellite)the_sat).cancelConstruction();
			choices_panel.removeAll();
			build.setEnabled(true);
			cancel.setEnabled(false);
		}
	}
	
	public void mouseEntered(MouseEvent e)
	{
		if(e.getSource() == base_icon)
			cost_label.setText(Integer.toString(GalacticStrategyConstants.BASE_MONEY_COST) + " money, " + Integer.toString(GalacticStrategyConstants.BASE_METAL_COST) + " metal");
		else if(e.getSource() == mine_icon)
			cost_label.setText(Integer.toString(GalacticStrategyConstants.MINE_MONEY_COST) + " money, " + Integer.toString(GalacticStrategyConstants.MINE_METAL_COST) + " metal");
		else if(e.getSource() == shipyard_icon)
			cost_label.setText(Integer.toString(GalacticStrategyConstants.SHIPYARD_MONEY_COST) + " money, " + Integer.toString(GalacticStrategyConstants.SHIPYARD_METAL_COST) + " metal");
	}
	
	public void mouseExited(MouseEvent e)
	{
		cost_label.setText(" ");
	}
	
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource()==base_icon)
			executeBuild(Facility.BASE);
		else if(e.getSource() == mine_icon)
			executeBuild(Facility.MINE);
		else if(e.getSource() == shipyard_icon)
			executeBuild(Facility.SHIPYARD);
	}
	
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}