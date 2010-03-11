import java.awt.event.*;

public class ShipBuilder implements MouseListener
{
	Shipyard yard;
	ShipType type;
	PlanetMoonCommandPanel panel;
	GameControl GC;
	
	public ShipBuilder(Shipyard s, ShipType t, PlanetMoonCommandPanel p, GameControl gc)
	{
		yard=s;
		type=t;
		panel=p;
		GC=gc;
	}
	
	
	public void mouseClicked(MouseEvent e)
	{
		yard.addToQueue(new Ship(type.name, type), GC.TC.getTime());
		panel.build_ship.setEnabled(true);
		panel.cancel_build_ship.setEnabled(false);
		panel.displayQueue();
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}