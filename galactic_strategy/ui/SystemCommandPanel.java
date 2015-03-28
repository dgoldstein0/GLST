package galactic_strategy.ui;
import galactic_strategy.game_objects.FacilityType;
import galactic_strategy.game_objects.Fleet;
import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.game_objects.Planet;
import galactic_strategy.game_objects.Satellite;
import galactic_strategy.game_objects.ShipType;

import java.awt.event.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

//Currently only supports Display for 2 players
public class SystemCommandPanel extends JPanel implements MouseListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3542119447543187278L;
	JPanel MyPlanets;
	JPanel MyShips;
	GSystem currentSystem;
	
	List<JProgressBar> buildingprog;
	//List<JProgressBar> shipprog; //of first shipyard in system
	List<JPanel> MyPlanets_list;
	List<Integer> buildindex;
	List<OwnableSatellite<?>> MPlanet_list;
	
	GroupLayout.ParallelGroup MyPlanets_vgroup;
	GroupLayout.SequentialGroup MyPlanets_hgroup;
	
	//NOTE: drawing assumes new buildings/Ships cannot be built from SystemCommandPanel
	SystemCommandPanel()
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		MyPlanets_list= new ArrayList<JPanel>();
		MPlanet_list = new ArrayList<OwnableSatellite<?>>();
		buildingprog = new ArrayList<JProgressBar>();
		buildindex = new ArrayList<Integer>();
		MyPlanets = new JPanel(new GridLayout(1,4));
		MyPlanets.setPreferredSize(new Dimension(600,140));
		MyShips = new JPanel();
	}
	
	public void setSystem(GSystem cur_System)
	{
		removeAll();
		buildindex.clear();
		buildingprog.clear();
		MyPlanets.removeAll();
		MyShips.removeAll();
		MyPlanets_list.clear();
		MPlanet_list.clear();
		OwnableSatellite<?> currentplanet;
		currentSystem = cur_System;
		int count=0;
		for(ListIterator<Satellite<?>> i = cur_System.getOrbiting().listIterator(); i.hasNext() && count<4;)
		{
			Satellite<?> testing = i.next();
			if(testing instanceof OwnableSatellite<?>)
			{		
				currentplanet=(OwnableSatellite<?>) testing;
				JPanel planet_panel= new JPanel();
				
				if((currentplanet.getOwner() != null) && (currentplanet.getOwner().getId() == GameInterface.GC.getPlayer_id()))
				{
					MyPlanets.add(planet_panel);
					BoxLayout block = new BoxLayout(planet_panel, BoxLayout.Y_AXIS);
					planet_panel.setLayout(block);
					MyPlanets_list.add(planet_panel);
					MPlanet_list.add(currentplanet);
					ImageIcon pic;
					if(currentplanet instanceof Planet)
						pic = new ImageIcon(ThumbPictResource.PLANET.Thumbnail);
					else
						pic = new ImageIcon(ThumbPictResource.MOON.Thumbnail);

					JLabel icon_label = new JLabel(pic);
					planet_panel.add(icon_label);
					JLabel name_label = new JLabel(currentplanet.getName());
					planet_panel.add(name_label);

					if(currentplanet.getBldg_in_progress() != FacilityType.NO_BLDG)
					{
						JLabel buildingpict = new JLabel(currentplanet.getBldg_in_progress().getIcon());
						planet_panel.add(buildingpict);
						JProgressBar xbuildingprog = new JProgressBar(0,1000);
						planet_panel.add(xbuildingprog);
						buildingprog.add( xbuildingprog);
						buildindex.add(buildingprog.indexOf(xbuildingprog));
					}
					/*if()
					JProgressBar manufac_bar = new JProgressBar(0,100);
					manufac_bar.setMaximumSize(new Dimension(120, 20));
					manufac_bar.setStringPainted(true);
					planet_panel.add(manufac_bar)*/
					planet_panel.addMouseListener(this);
					planet_panel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
					count++;
				}
			}

		}
		while(count<4)
		{
			MyPlanets.add(Box.createRigidArea(new Dimension (0,0)));
			count++;
		}
		
		Fleet.ShipIterator ship_iter = cur_System.getFleets()[GameInterface.GC.getPlayer_id()].iterator();

		if(ship_iter.hasNext()){
			MyShips.add(new JLabel(ShipType.JUNK.getIcon()));
			MyShips.add(new JLabel("Select All"));
			BoxLayout bl = new BoxLayout(MyShips, BoxLayout.Y_AXIS);
			MyShips.setLayout(bl);
			MyShips.addMouseListener(this);
			MyShips.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		}
		
		add(MyShips);
		//update();
	}
	
	public void update(long t)
	{
		ListIterator<Integer> k = buildindex.listIterator();
		while(k.hasNext())
		{
			int index = k.next();
			OwnableSatellite<?> currentplanet = MPlanet_list.get(index);
			JProgressBar progress = buildingprog.get(index);
			if(currentplanet.getBldg_in_progress() != FacilityType.NO_BLDG)
			{
				double prog = currentplanet.constructionProgress(t);
				progress.setValue((int)(1000.0*prog));
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
		if(arg0.getSource()==MyShips)
		{
			GameInterface.GC.GI.selected_in_sys.clear();
			GameInterface.GC.GI.maybe_select_in_sys.clear();
			Fleet users_fleet = currentSystem.getFleets()[GameInterface.GC.getPlayer_id()];
			for (Fleet.ShipIterator j = users_fleet.iterator(); j.hasNext();)
			{
				GameInterface.GC.GI.selected_in_sys.add(
					users_fleet.getShips().get(j.next())
				);
			}
			GameInterface.GC.GI.refreshShipPanel();
		}
		else
		{
			for (int i=0; i < MyPlanets_list.size(); i++)
			{
				if(MyPlanets_list.get(i) == arg0.getSource())
				{
					GameInterface.GC.GI.selectObjInSystem(MPlanet_list.get(i));
					return;
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
