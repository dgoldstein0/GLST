package galactic_strategy.ui;

import galactic_strategy.game_objects.AbstractDestination;

public class DestDisplayUpdater implements Runnable
{
	final AbstractDestination<?> the_sat;
	
	public DestDisplayUpdater(AbstractDestination<?> sat)
	{
		the_sat = sat;
	}
	
	public void run()
	{
		if (GameInterface.GC.GI != null)
		{
			ShipCommandPanel panel = GameInterface.GC.GI.ShipPanel;
			if(panel.the_ship != null && panel.the_ship.getDestination() == the_sat)
			{
				panel.updateDestDisplay(panel.the_ship.getDestination());
			}
		}
	}
}