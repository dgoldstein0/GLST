import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class SystemLabel extends JLabel implements MouseListener
{
	GSystem the_sys;
	GameInterface the_interface;
	
	public SystemLabel(GSystem s, GameInterface g_interface)
	{		
		super(s.name);
		the_interface=g_interface;
		the_sys =s;		
		this.addMouseListener(this);		
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub		
		if (arg0.getClickCount()==2)
		{
			the_interface.satellites_list.removeAll();
			for (Satellite satellite: the_sys.orbiting_objects)
			{
				SatelliteLabel label=new SatelliteLabel(satellite);
				the_interface.satellites_list.add(label);			
			}			
		}
	}

	
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub		
		the_interface.sys=the_sys;		
		the_interface.drawSystem();	


	}


	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
