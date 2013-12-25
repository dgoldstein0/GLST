package galactic_strategy.ui;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.game_objects.Planet;
import galactic_strategy.game_objects.StellarObject;

import javax.swing.*;

import java.awt.event.*;
import java.awt.Color;
import java.util.ArrayList;

public class StartLocationsDialog extends JDialog implements ActionListener, MouseListener
{
	private static final long serialVersionUID = 8228591722126679392L;
	
	JButton add_place, remove_place;
	JPanel the_list;
	SatelliteLabel selected;
	GDFrame GDF;
	
	public StartLocationsDialog(GDFrame gdf, JFrame frame)
	{
		super(frame, "Starting Locations", false);
		GDF=gdf;
		
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		
		JPanel button_panel = new JPanel();
		
		add_place = new JButton("Add");
		add_place.addActionListener(this);
		button_panel.add(add_place);
		
		remove_place = new JButton("Remove");
		remove_place.addActionListener(this);
		remove_place.setEnabled(false);
		button_panel.add(remove_place);
		
		panel.add(button_panel);
		
		the_list = new JPanel();
		BoxLayout layout2 = new BoxLayout(the_list, BoxLayout.Y_AXIS);
		the_list.setLayout(layout2);
		
		for(OwnableSatellite<?> sat : GDF.map.getStart_locations())
		{
			SatelliteLabel label = new SatelliteLabel(sat);
			label.addMouseListener(this);
			the_list.add(label);
		}
		
		panel.add(the_list);
		
		add(panel);
		
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == add_place)
			addItem();
		else if(e.getSource() == remove_place)
			removeItem();
	}
	
	public void addItem(){
		if(GDF.sysview.selected_obj instanceof StellarObject)
		{
			Selectable obj = GDF.sysview.selected_obj;
			if(obj instanceof Planet)
			{
				ArrayList<OwnableSatellite<?>> start_locations = GDF.map.getStart_locations();
				if(!start_locations.contains((Planet) obj))
				{
					start_locations.add((Planet) obj);
					SatelliteLabel label = new SatelliteLabel((Planet) obj);
					label.addMouseListener(this);
					the_list.add(label);
					select(label);
					pack();
				}
			}
		}
	}
	
	public void removeItem() {
		if(selected != null)
		{
			GDF.map.getStart_locations().remove(selected.the_sat);
			the_list.remove(selected);
			remove_place.setEnabled(false);
			pack();
		}
	}
	
	private void select(SatelliteLabel l)
	{
		if(selected != null)
			selected.setForeground(Color.BLACK);
		selected=l;
		selected.setForeground(Color.RED);
		remove_place.setEnabled(true);
	}
	
	public void mouseExited(MouseEvent e){
	}
	
	public void mouseEntered(MouseEvent e){
	}
	
	public void mouseClicked(MouseEvent e){
		select((SatelliteLabel) e.getSource());
	}
	
	public void mousePressed(MouseEvent e){
	}
	
	public void mouseReleased(MouseEvent e){
	}
}