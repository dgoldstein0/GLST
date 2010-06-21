import java.awt.event.*;
import java.awt.Color;
import javax.swing.JPanel;

public class ShipBuilder implements MouseListener
{
	Shipyard yard;
	ShipType type;
	PlanetMoonCommandPanel panel;
	JPanel type_panel;
	boolean click_on;
	boolean was_clicked_on;
	
	public ShipBuilder(Shipyard s, ShipType t, JPanel typepanel, PlanetMoonCommandPanel p)
	{
		yard=s;
		type=t;
		panel=p;
		type_panel=typepanel;
		
		type_panel.setOpaque(false);
		type_panel.setBackground(new Color(150,150,255));
	}
	
	
	public void mouseReleased(MouseEvent e)
	{
		if(click_on)
		{
			if(yard.addToQueue(new Ship(type), GameInterface.GC.TC.getTime()))
			{
				if(!e.isShiftDown())
				{
					panel.build_ship.setEnabled(true);
					panel.displayQueue();
				}
			}
			else
				SoundManager.playSound("sound/doot doot.wav");
			type_panel.setOpaque(false);
			type_panel.repaint();
		}
		was_clicked_on=false;
	}
	
	public void mouseEntered(MouseEvent e)
	{
		if(was_clicked_on)
		{
			type_panel.setOpaque(true);
			type_panel.repaint();
			click_on=true;
		}
	}
	
	public void mouseExited(MouseEvent e)
	{
		type_panel.setOpaque(false);
		type_panel.repaint();
		click_on=false;
	}
	
	public void mousePressed(MouseEvent e)
	{
		type_panel.setOpaque(true);

		type_panel.repaint();
		click_on=true;
		was_clicked_on=true;
	}
	public void mouseClicked(MouseEvent e){}
}