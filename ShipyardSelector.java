import java.awt.event.*;

public class ShipyardSelector implements MouseListener
{
	Shipyard the_shipyard;
	PlanetMoonCommandPanel panel;
	
	public ShipyardSelector(Shipyard s, PlanetMoonCommandPanel p)
	{
		the_shipyard=s;
		panel=p;
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	public void mousePressed(MouseEvent e)
	{
		panel.shipyardDetails(the_shipyard);
	}
}