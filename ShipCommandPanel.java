import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ShipCommandPanel extends JPanel implements ActionListener
{
	Ship the_ship;
	GameInterface GI;
	
	JProgressBar health;
	
	JPanel button_panel;
	JButton move;
	JButton warp;
	JButton invade;
	JButton pickup_troops;
	JLabel soldier_label;
	
	public ShipCommandPanel(GameInterface gi)
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		GI=gi;
		
		//build the buttons
		button_panel = new JPanel(new GridLayout(5,1));
		
		move=new JButton("Move/Attack");
		move.addActionListener(this);
		button_panel.add(move);
		
		warp = new JButton("Warp");
		warp.addActionListener(this);
		button_panel.add(warp);
		
		invade = new JButton("Invade");
		invade.addActionListener(this);
		button_panel.add(invade);
		
		pickup_troops = new JButton("Pickup Troops...");
		pickup_troops.addActionListener(this);
		button_panel.add(pickup_troops);
		
		soldier_label = new JLabel();
		button_panel.add(soldier_label);
	}
	
	public void setShip(Ship s)
	{
		removeAll();
		the_ship=s;
		
		JPanel pic_panel = new JPanel();
		GroupLayout pic_layout = new GroupLayout(pic_panel);
		pic_panel.setLayout(pic_layout);
		add(pic_panel);
		
		GroupLayout.ParallelGroup pic_hgroup = pic_layout.createParallelGroup();
		GroupLayout.SequentialGroup pic_vgroup = pic_layout.createSequentialGroup();
		
		pic_layout.setHorizontalGroup(pic_hgroup);
		pic_layout.setVerticalGroup(pic_vgroup);
		
		JLabel name_label = new JLabel(s.getName());
		JPanel name_panel = new JPanel();
		name_panel.setBackground(s.getOwner().getColor());
		name_panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		name_panel.add(name_label);
		pic_vgroup.addComponent(name_panel);
		pic_hgroup.addComponent(name_panel);
		
		ImageIcon pic = new ImageIcon(s.type.img);
		JLabel icon_label = new JLabel(pic);
		pic_vgroup.addComponent(icon_label);
		pic_hgroup.addComponent(icon_label);
		
		health = new JProgressBar(0, s.hull_strength);
		health.setMaximumSize(new Dimension(100,20));
		health.setStringPainted(true);
		pic_vgroup.addComponent(health);
		pic_hgroup.addComponent(health);
		
		update();
		add(button_panel);
	}
	
	public void update()
	{
		health.setValue(the_ship.hull_strength - the_ship.damage);
		health.setString(Integer.toString(the_ship.hull_strength - the_ship.damage));
		soldier_label.setText("    " + Integer.toString(the_ship.getSoldierInt()) + " soldiers");
		
		if(the_ship.destination instanceof OwnableSatellite && Math.hypot(the_ship.dest_x_coord-the_ship.pos_x, the_ship.dest_y_coord-the_ship.pos_y) <= GalacticStrategyConstants.LANDING_RANGE)
		{
			if(((OwnableSatellite)the_ship.destination).getOwner() instanceof Player && ((OwnableSatellite)the_ship.destination).getOwner().getId() == GI.GC.player_id)
			{
				pickup_troops.setEnabled(true);
				invade.setEnabled(false);
			}
			else
			{
				pickup_troops.setEnabled(false);
				invade.setEnabled(true);
			}
		}
		else
		{
			pickup_troops.setEnabled(false);
			invade.setEnabled(false);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == move)
		{
			GI.switchSystemToDestinationMode();
		}
		else if(e.getSource() == warp)
		{
			
		}
		else if(e.getSource() == invade)
		{
			if(((OwnableSatellite)the_ship.destination).getOwner() instanceof Player)
			{
				boolean base_seen=false;
				synchronized(((OwnableSatellite)the_ship.destination).facilities_lock)
				{
					for(Facility f : ((OwnableSatellite)the_ship.destination).facilities)
					{
						if(f instanceof Base) //there should only be one base on the planet
						{
							((Base)f).attackedByTroops(GI.GC.TC.getTime(), the_ship);
							base_seen=true;
						}
					}
				}
				if(!base_seen) //if base isn't finished being built, player can take over without a fight
					((OwnableSatellite)the_ship.destination).setOwner(the_ship.getOwner());
			}
			else
			{
				((OwnableSatellite)the_ship.destination).setOwner(the_ship.getOwner());
			}
		}
		else if(e.getSource() == pickup_troops)
		{
			synchronized(((OwnableSatellite)the_ship.destination).facilities_lock)
			{
				for(Facility f : ((OwnableSatellite)the_ship.destination).facilities)
				{
					if(f instanceof Base) //there should only be one base on the planet
					{
						the_ship.soldier += ((Base)f).retrieveSoldiers(GI.GC.TC.getTime(),the_ship.type.soldier_capacity - the_ship.soldier);
						break;
					}
				}
			}
		}
	}
}