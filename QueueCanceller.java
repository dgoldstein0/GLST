import java.awt.event.*;

public class QueueCanceller implements ActionListener
{
	Shipyard yard;
	Ship the_ship;
	
	public QueueCanceller(PlanetMoonCommandPanel p, Shipyard sy, Ship s)
	{
		yard =sy;
		the_ship = s;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		GameInterface.GC.scheduleOrder(new ShipyardCancelBuildOrder(yard, the_ship, GameInterface.GC.updater.getTime()));
	}
}