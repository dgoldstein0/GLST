import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ShipCommandPanel extends JPanel implements ActionListener
{
	Ship the_ship;
	
	JProgressBar health;
	
	JPanel button_panel;
	JButton move;
	JButton warp;
	JButton invade;
	JButton pickup_troops;
	JLabel soldier_label;
	
	JPanel dest_display;
	JPanel dest_pic_panel;
	JPanel dest_name_panel;
	JLabel dest_name;
	JLabel dest_pic;
	
	public ShipCommandPanel()
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		//build the buttons
		button_panel = new JPanel(new GridLayout(5,1));
		
		move=new JButton("Move");
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
		
		//set up destination display
		dest_display = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		dest_pic_panel = new JPanel();
		BoxLayout pic_layout = new BoxLayout(dest_pic_panel, BoxLayout.Y_AXIS);
		dest_pic_panel.setLayout(pic_layout);
		
		dest_name = new JLabel();
		dest_name_panel = new JPanel();
		dest_name_panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		dest_name_panel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		dest_name_panel.add(dest_name);
		dest_pic_panel.add(dest_name_panel);
		
		dest_pic = new JLabel();
		dest_pic.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		dest_pic_panel.add(dest_pic);
		
		dest_display.add(dest_pic_panel);
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
		add(dest_display);
		updateDestDisplay();
	}
	
	public void update()
	{
		health.setValue(the_ship.hull_strength - the_ship.damage);
		health.setString(Integer.toString(the_ship.hull_strength - the_ship.damage));
		soldier_label.setText("    " + Integer.toString(the_ship.getSoldierInt()) + " soldiers");
		
		if(the_ship.destination instanceof OwnableSatellite && Math.hypot(the_ship.dest_x_coord-the_ship.pos_x, the_ship.dest_y_coord-the_ship.pos_y) <= GalacticStrategyConstants.LANDING_RANGE)
		{
			if(((OwnableSatellite)the_ship.destination).getOwner() instanceof Player && ((OwnableSatellite)the_ship.destination).getOwner().getId() == GameInterface.GC.player_id)
			{
				if(((OwnableSatellite)the_ship.destination).the_base instanceof Base && the_ship.soldier < the_ship.type.soldier_capacity)
					pickup_troops.setEnabled(true);
				else
					pickup_troops.setEnabled(false);
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
	
	public void updateDestDisplay()
	{
		if(the_ship.destination instanceof OwnableSatellite && ((OwnableSatellite)the_ship.destination).owner instanceof Player)
		{//color background appropriately
			dest_name_panel.setBackground(((OwnableSatellite)the_ship.destination).owner.getColor());
			dest_name_panel.setOpaque(true);
		}
		else
			dest_name_panel.setOpaque(false); //show no background
		dest_name_panel.repaint(); //force redraw.  this will force the background to be redrawn, so blank pixels become colored or vice-versa
		dest_name.setText(the_ship.destination.getName());
		dest_pic.setIcon(new ImageIcon(the_ship.destination.imageLoc()));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == move)
		{
			GameInterface.GC.GI.switchSystemToDestinationMode();
			the_ship.attacking=false;
		}
		else if(e.getSource() == warp)
		{
			the_ship.attacking=false;
		}
		else if(e.getSource() == invade)
		{
			the_ship.attacking=false;
			if(((OwnableSatellite)the_ship.destination).getOwner() instanceof Player)
			{
				boolean base_seen=false;
				synchronized(((OwnableSatellite)the_ship.destination).facilities_lock)
				{
					for(Facility f : ((OwnableSatellite)the_ship.destination).facilities)
					{
						if(f instanceof Base) //there should only be one base on the planet
						{
							((Base)f).attackedByTroops(GameInterface.GC.TC.getTime(), the_ship);
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
			updateDestDisplay();
		}
		else if(e.getSource() == pickup_troops)
		{
			synchronized(((OwnableSatellite)the_ship.destination).facilities_lock)
			{
				the_ship.soldier += ((OwnableSatellite)the_ship.destination).the_base.retrieveSoldiers(GameInterface.GC.TC.getTime(),the_ship.type.soldier_capacity - the_ship.soldier);
			}
		}
	}
}