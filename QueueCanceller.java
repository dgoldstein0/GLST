import java.awt.event.*;

public class QueueCanceller implements ActionListener
{
	PlanetMoonCommandPanel panel;
	Shipyard yard;
	Ship the_ship;
	
	public QueueCanceller(PlanetMoonCommandPanel p, Shipyard sy, Ship s)
	{
		panel=p;
		yard =sy;
		the_ship = s;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		yard.removeFromQueue(the_ship, GameInterface.GC.TC.getTime());
		panel.displayQueue();
	}
}