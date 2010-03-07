import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class PlanetMoonCommandPanel extends JPanel implements ActionListener
{
	Satellite the_sat;
	JButton build;
	JLabel pop;
	
	//for facility-building progress
	JProgressBar progress_bar;
	
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
		
		progress_bar = new JProgressBar(0,100);
	}
	
	public void setSat(Satellite s)
	{
		the_sat=s;

		//now update the panel
		removeAll();
		
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
		stats_panel.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);
		add(stats_panel);
		
		if(s instanceof OwnableSatellite)
		{
			pop = new JLabel("Population: " + ((OwnableSatellite)s).getPopulation());
			stats_panel.add(pop);
			
			if(((OwnableSatellite)s).getOwner() instanceof Player)
			{
				//color if there is an owner
				name_panel.setBackground(((OwnableSatellite)s).getOwner().getColor());
				
				//if you are the owner, commands!
				if(((OwnableSatellite)s).getOwner().getId() == GC.player_id)
				{
					stats_panel.add(build);
					stats_panel.add(progress_bar);
				}
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
			if(((OwnableSatellite)the_sat).bldg_in_progress != OwnableSatellite.NO_BLDG)
			{
				double prog = ((OwnableSatellite)the_sat).constructionProgress(t);
				progress_bar.setValue((int)(100.0*prog));
			}
			else
				progress_bar.setValue(0);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == build)
		{
			((OwnableSatellite)the_sat).scheduleConstruction(OwnableSatellite.MINE, GalacticStrategyConstants.MINE_BUILD_TIME, GC.TC.getTime());
		}
	}
}