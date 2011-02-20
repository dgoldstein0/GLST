import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;


public class SystemLabel extends JLabel implements MouseListener
{
	private static final long serialVersionUID = -6685546432555351721L;
	
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
			
		if (arg0.getClickCount()>=1)
		{			
			the_interface.prev_mode=GameInterface.GalaxyOrSystem.System;
			the_interface.prev_sys=the_sys;	
			the_interface.selected_sys.clear();
			the_interface.selected_sys.add(the_sys);		
			the_interface.prev_selected=the_interface.selected_in_sys;
			
			the_interface.displayNoPanel();
			
			if (arg0.getClickCount()==2)
			{
				the_interface.tabbedPane.setSelectedIndex(1);			
				the_interface.sys=the_sys;					
			}
		}	
	}
	
	public void mouseEntered(MouseEvent arg0) {
				
		if (the_sys!=the_interface.sys)
		{
			the_interface.prev_selected=the_interface.selected_in_sys;
			the_interface.selected_in_sys=new ArrayList<Selectable>();
		}	
		else
		{
			the_interface.prev_selected=the_interface.selected_in_sys;
		}		
		
		the_interface.prev_mode = the_interface.mode;
		the_interface.prev_sys=the_interface.sys;
		the_interface.sys=the_sys;
		the_interface.prev_scale=the_interface.sys_scale;
		the_interface.prev_x=the_interface.sys_center_x;;
		the_interface.prev_y=the_interface.sys_center_y;
		the_interface.drawSystem(the_interface.prev_mode == GameInterface.GalaxyOrSystem.System);
	}


	public void mouseExited(MouseEvent arg0) {
				
		the_interface.selected_in_sys=the_interface.prev_selected;
				
		if (the_interface.prev_sys!=null)
		{
			switch(the_interface.prev_mode)
			{
				case System:
					if(the_interface.sys != the_interface.prev_sys)
					{
						the_interface.sys=the_interface.prev_sys;
						the_interface.sys_scale=the_interface.prev_scale;
						the_interface.sys_center_x=the_interface.prev_x;
						the_interface.sys_center_y=the_interface.prev_y;
					}
					the_interface.drawSystem(true);
					break;
				case Galaxy:
					the_interface.drawGalaxy(GameInterface.GALAXY_STATE.NORMAL);
					break;
			}
		}
		else
		{
			the_interface.drawGalaxy(GameInterface.GALAXY_STATE.NORMAL);
		}
	}


	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void update(long t) {
		
		setForeground(replaceWhiteWithBlack(the_sys.currentColor(t)));
	}
	
	public static Color replaceWhiteWithBlack(Color in)
	{
		if(in.equals(Color.WHITE))
			return Color.BLACK;
		else
			return in;
	}
}
