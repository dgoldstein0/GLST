import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.*;

import javax.swing.*;

//Currently only supports Display for 2 players
public class SystemCommandPanel extends JPanel implements MouseListener
{

	JPanel MyPlanets;
	JPanel MyShips;
	GSystem currentSystem;
	
	List<JPanel> MyPlanets_list;

	List<Selectable> MPlanet_list;

	GroupLayout.ParallelGroup MyPlanets_vgroup;
	GroupLayout.SequentialGroup MyPlanets_hgroup;

	
	SystemCommandPanel()
	{
		super();
		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(layout);
		//SystemInfo = new JPanel(new GridLayout(4,1));
		
		MyPlanets_list= new ArrayList<JPanel>();
		MPlanet_list = new ArrayList<Selectable>();
		MyPlanets = new JPanel(new GridLayout(2,2));
		MyShips = new JPanel();

		GroupLayout MyPlanets_layout = new GroupLayout(MyPlanets);
		MyPlanets.setLayout(MyPlanets_layout);
		
		MyPlanets_vgroup = MyPlanets_layout.createParallelGroup();
		MyPlanets_hgroup = MyPlanets_layout.createSequentialGroup();
		
		MyPlanets_layout.setHorizontalGroup(MyPlanets_hgroup);
		MyPlanets_layout.setVerticalGroup(MyPlanets_vgroup);
		//SystemInfo.add(MyPlanets);
	}
	public void setSystem(GSystem cur_System)
	{
		removeAll();
		MyPlanets.removeAll();
		MyShips.removeAll();
		MyPlanets_list.clear();
		MPlanet_list.clear();
		OwnableSatellite<?> currentplanet;
		currentSystem = cur_System;
		int count=0;
		for(ListIterator<Satellite<?>> i =cur_System.orbiting.listIterator();i.hasNext()&&count<4;)
		{
			Satellite<?> testing = i.next();
			if(testing instanceof OwnableSatellite<?>)
			{
				currentplanet=(OwnableSatellite<?>) testing;
				JPanel planet_panel= new JPanel();
				if((currentplanet.getOwner()!=null) && (currentplanet.getOwner().getId()==GameInterface.GC.player_id))
				{
					MyPlanets_vgroup.addComponent(planet_panel);
					MyPlanets_hgroup.addComponent(planet_panel);
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
					planet_panel.addMouseListener(this);
					planet_panel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
					planet_panel.setPreferredSize(new Dimension(75,65));
					count++;
				}
			}

		}

		//add(SystemInfo);
		add(MyPlanets);
		MyShips.add(new JLabel(ShipType.JUNK.icon));
		MyShips.addMouseListener(this);
		add(MyShips);
		update();
	}
	
	public void update()
	{

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getSource()==MyShips)
			{
				GameInterface.GC.GI.selected_in_sys.clear();
				GameInterface.GC.GI.maybe_select_in_sys.clear();
				for(Fleet.ShipIterator j=currentSystem.fleets[GameInterface.GC.player_id].iterator();j.hasNext();)
				{
					GameInterface.GC.GI.selected_in_sys.add(currentSystem.fleets[GameInterface.GC.player_id].ships.get(j.next()));
				}
				GameInterface.GC.GI.refreshShipPanel();
			}
		else
		{
			for(int i=0;i<MyPlanets_list.size();i++ )
			{
				if(MyPlanets_list.get(i)==arg0.getSource())
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
