package galactic_strategy.ui;

import galactic_strategy.Constants;
import galactic_strategy.game_objects.Ship;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class FormationCommandPanel extends JPanel implements MouseListener {

	private static final long serialVersionUID = -38910054303467084L;
	
	List<Ship> ship_list;
	List<JProgressBar> health_list;
	List<JProgressBar> soldier_list;
	List<JPanel> ship_panel_list;
	JPanel ship_fleet_panel;
	
	public FormationCommandPanel() {
		health_list = new ArrayList<JProgressBar>();
		soldier_list = new ArrayList<JProgressBar>();
		ship_panel_list = new ArrayList<JPanel>();
		ship_fleet_panel = new JPanel(new GridLayout(2,6));
	}
	
	public void setShips(List<Ship> selected) {
		ship_list = selected;
		
		ship_fleet_panel.removeAll();
		health_list.clear();
		soldier_list.clear();
		ship_panel_list.clear();
		
		if (selected.size() > 1) {
			ListIterator<Ship> ship_iter = selected.listIterator();
			JProgressBar healthofship;
			int index=0;
			while(ship_iter.hasNext() && index < 12)
			{
				Ship current = ship_iter.next();
				JPanel the_panel2 = new JPanel();
				ship_panel_list.add(the_panel2);
				BoxLayout bl = new BoxLayout(the_panel2, BoxLayout.Y_AXIS);
				the_panel2.setLayout(bl);
				ImageIcon shippict = current.getType().getIcon();
				the_panel2.add(new JLabel(shippict));
				the_panel2.addMouseListener(this);
				healthofship = new JProgressBar(0, current.getType().hull);
				healthofship.setPreferredSize(new Dimension(Constants.mini_prog_w,10));
				healthofship.setStringPainted(true);
				health_list.add(healthofship);
				the_panel2.add(health_list.get(index));
				JProgressBar soldier_label = new JProgressBar(0,current.getType().soldier_capacity);
				soldier_label.setPreferredSize(new Dimension(Constants.mini_prog_w,10));
				soldier_label.setForeground(Color.blue);
				soldier_label.setStringPainted(true);
				soldier_list.add(soldier_label);
				the_panel2.add(soldier_list.get(index));
				the_panel2.setBorder(BorderFactory.createLineBorder(Color.GREEN));
				the_panel2.setPreferredSize(new Dimension(Constants.mini_ship_w,Constants.mini_ship_h));
				index++;
				ship_fleet_panel.add(the_panel2);
			}
			if(index<12)
			{
				ship_fleet_panel.add(Box.createRigidArea(new Dimension(Constants.mini_ship_w,Constants.mini_ship_h)));
				index++;
			}
			add(ship_fleet_panel);
		}
	}
	
	public void update() {
		ListIterator<JProgressBar> health_iter = health_list.listIterator();
		ListIterator<Ship> ship_iter = ship_list.listIterator();
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
			healthofship.setValue(current.getType().hull - current.getDamage());
			healthofship.setString(Integer.toString(current.getType().hull - current.getDamage()));
			soldierlabelship.setValue(current.getSoldierInt());
			soldierlabelship.setString(Integer.toString((current.getSoldierInt())));
			count++;
		}
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
		for(int i=0;i<ship_panel_list.size();i++ )
		{
			if(ship_panel_list.get(i)==arg0.getSource())
			{
				GameInterface.GC.GI.selectObjInSystem(ship_list.get(i));
				break;
			}
		}
	}
	@Override public void mouseEntered(MouseEvent arg0) {}
	@Override public void mouseExited(MouseEvent arg0) {}
	@Override public void mousePressed(MouseEvent arg0) {}
	@Override public void mouseReleased(MouseEvent arg0) {}
}
