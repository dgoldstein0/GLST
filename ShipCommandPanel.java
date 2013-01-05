import javax.swing.*;

import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.*;





public class ShipCommandPanel extends JPanel implements ActionListener, MouseListener
{
	Ship the_ship;
	List<Selectable> ship_list;
	JProgressBar health;
	List<JProgressBar> health_list;
	List<JProgressBar> soldier_list;
	List<JPanel> ship_panel_list;
	JPanel button_panel;
	JPanel ship_fleet_panel;
	JButton attack;
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
		health_list = new ArrayList<JProgressBar>();
		soldier_list = new ArrayList<JProgressBar>();
		ship_panel_list = new ArrayList<JPanel>();
		ship_fleet_panel = new JPanel(new GridLayout(2,6));
		//build the buttons
		button_panel = new JPanel(new GridLayout(5,1));
		
		attack=new JButton("Attack");
		attack.addActionListener(this);
		button_panel.add(attack);
		
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
	
	public void setShip(Ship s, List<Selectable> selected)
	{
		removeAll();
		ship_fleet_panel.removeAll();
		health_list.clear();
		soldier_list.clear();
		ship_panel_list.clear();
		the_ship=s;
		ship_list=selected;
		//toggle buttons
		Ship current;
		boolean enable = (the_ship.owner.getId() == GameInterface.GC.player_id);
		
		attack.setEnabled(enable);
		move.setEnabled(enable);
		warp.setEnabled(enable);
		
		
		
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
		
		ImageIcon pic = s.type.icon;
		JLabel icon_label = new JLabel(pic);
		pic_vgroup.addComponent(icon_label);
		pic_hgroup.addComponent(icon_label);
		
		health = new JProgressBar(0, s.type.hull);
		health.setMaximumSize(new Dimension(100,20));
		health.setStringPainted(true);
		pic_vgroup.addComponent(health);
		pic_hgroup.addComponent(health);
		
		update();		
		add(button_panel);
		add(dest_display);
		updateDestDisplay(s.destination);
		if(selected.size()>1){
			ListIterator<Selectable> ship_iter = selected.listIterator();
			JProgressBar healthofship;
			int index=0;
			while(ship_iter.hasNext()&&index<12)
			{
				current= (Ship)ship_iter.next();
				JPanel the_panel2 = new JPanel();
				ship_panel_list.add(the_panel2);
				BoxLayout bl = new BoxLayout(the_panel2, BoxLayout.Y_AXIS);
				the_panel2.setLayout(bl);
				ImageIcon shippict = new ImageIcon(current.type.thumbimg.Thumbnail);
				the_panel2.add(new JLabel(shippict));
				the_panel2.addMouseListener(this);
				healthofship= new JProgressBar(0,current.type.hull);
				healthofship.setPreferredSize(new Dimension(GalacticStrategyConstants.mini_prog_w,10));
				healthofship.setStringPainted(true);
				health_list.add(healthofship);
				the_panel2.add(health_list.get(index));
				JProgressBar soldier_label = new JProgressBar(0,current.type.soldier_capacity);
				soldier_label.setPreferredSize(new Dimension(GalacticStrategyConstants.mini_prog_w,10));
				soldier_label.setForeground(Color.blue);
				soldier_label.setStringPainted(true);
				soldier_list.add(soldier_label);
				the_panel2.add(soldier_list.get(index));
				the_panel2.setBorder(BorderFactory.createLineBorder(Color.GREEN));
				the_panel2.setPreferredSize(new Dimension(GalacticStrategyConstants.mini_ship_w,GalacticStrategyConstants.mini_ship_h));
				if(current==s)the_panel2.setBorder(BorderFactory.createLineBorder(Color.RED));
				index++;
				ship_fleet_panel.add(the_panel2);
			}
			if(index<12)
			{
				ship_fleet_panel.add(Box.createRigidArea(new Dimension(GalacticStrategyConstants.mini_ship_w,GalacticStrategyConstants.mini_ship_h)));
				index++;
			}
			add(ship_fleet_panel);
		}
	}
	
	public void update()
	{	
		health.setValue(the_ship.type.hull - the_ship.damage);
		health.setString(Integer.toString(the_ship.type.hull - the_ship.damage));
		
		soldier_label.setText("  " + Integer.toString(the_ship.getSoldierInt()) + " soldiers");
		if(ship_list.size()>1)
		{
			ListIterator<JProgressBar> health_iter = health_list.listIterator();
			ListIterator<Selectable> ship_iter = ship_list.listIterator();
			ListIterator<JProgressBar> label_iter = soldier_list.listIterator();
			JProgressBar soldierlabelship;
			JProgressBar healthofship;
			Ship current;
			int count =0;
			while(health_iter.hasNext()&&ship_iter.hasNext()&&count<12)
			{
				current = (Ship)ship_iter.next();
				healthofship=health_iter.next();
				soldierlabelship=label_iter.next();
				healthofship.setValue(current.type.hull - current.damage);
				healthofship.setString(Integer.toString(current.type.hull - current.damage));
				soldierlabelship.setValue(current.getSoldierInt());
				soldierlabelship.setString(Integer.toString((current.getSoldierInt())));
				count++;
			}
		}
		
		if(the_ship.destination instanceof OwnableSatellite<?>&& the_ship.owner.getId() == GameInterface.GC.player_id && Math.hypot(the_ship.dest_x_coord-the_ship.pos_x, the_ship.dest_y_coord-the_ship.pos_y) <= GalacticStrategyConstants.LANDING_RANGE)
		{
			if(((OwnableSatellite<?>)the_ship.destination).getOwner() != null && ((OwnableSatellite<?>)the_ship.destination).getOwner().getId() == GameInterface.GC.player_id)
			{
				if(((OwnableSatellite<?>)the_ship.destination).the_base != null && the_ship.soldier < the_ship.type.soldier_capacity)
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
	
	public void updateDestDisplay(Destination<?> d)
	{
		if(d instanceof OwnableSatellite<?> && ((OwnableSatellite<?>)d).owner instanceof Player)
		{//color background appropriately
			dest_name_panel.setBackground(((OwnableSatellite<?>)d).owner.getColor());
			dest_name_panel.setOpaque(true);
		}
		else
			dest_name_panel.setOpaque(false); //show no background
		dest_name_panel.repaint(); //force redraw.  this will force the background to be redrawn, so blank pixels become colored or vice-versa
		dest_name.setText(d.getName());
		dest_pic.setIcon(new ImageIcon(d.imageLoc()));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == attack)
		{
			GameInterface.GC.GI.switchSystemToAttackMoveDestinationMode();
		}
		else if(e.getSource() == move)
		{
			GameInterface.GC.GI.switchSystemToDestinationMode();
		}
		else if(e.getSource() == warp)
		{
			GameInterface.GC.GI.drawGalaxy(GameInterface.GALAXY_STATE.CHOOSE_WARP_DEST);
		}
		else if(e.getSource() == invade)
		{
			GameInterface.GC.scheduleOrder(
				new ShipInvadeOrder(GameInterface.GC.players[GameInterface.GC.player_id], the_ship, GameInterface.GC.updater.TC.getNextTimeGrain())
			);
		}
		else if(e.getSource() == pickup_troops)
		{
			GameInterface.GC.scheduleOrder(
				new ShipPickupTroopsOrder(GameInterface.GC.players[GameInterface.GC.player_id], the_ship, GameInterface.GC.updater.TC.getNextTimeGrain())
			);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
		for(int i=0;i<ship_panel_list.size();i++ )
		{
			if(ship_panel_list.get(i)==arg0.getSource())
			{
				GameInterface.GC.GI.selectObjInSystem(ship_list.get(i));
				break;
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