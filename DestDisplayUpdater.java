public class DestDisplayUpdater implements Runnable
{
	final Destination<?> the_sat;
	
	public DestDisplayUpdater(Destination<?> sat)
	{
		the_sat = sat;
	}
	
	public void run()
	{
		if (GameInterface.GC.GI != null)
		{
			ShipCommandPanel panel = GameInterface.GC.GI.ShipPanel;
			if(panel.the_ship != null && panel.the_ship.destination == the_sat)
			{
				panel.updateDestDisplay(panel.the_ship.destination);
			}
		}
	}
}