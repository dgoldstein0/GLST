package galactic_strategy.ui;
import galactic_strategy.game_objects.Shipyard;


public class QueueUpdater implements Runnable {

	Shipyard yard;
	
	public QueueUpdater(Shipyard sy)
	{
		yard =sy;
	}
	
	@Override
	public void run()
	{
		//System.out.println("update queue");
		if (GameInterface.GC.GI != null)
		{
			PlanetMoonCommandPanel panel = GameInterface.GC.GI.SatellitePanel;
			if(panel != null && yard.getOwner().getId() == GameInterface.GC.getPlayer_id()
					&& GameInterface.GC.GI.displayed_control_panel == GameInterface.PANEL_DISP.SAT_PANEL
					&& panel.the_sat == yard.getLocation()
					&& (panel.state == PlanetMoonCommandPanel.PANEL_STATE.SHIP_QUEUE_DISPLAYED || 
							(panel.state == PlanetMoonCommandPanel.PANEL_STATE.SHIP_CHOICES_DISPLAYED
								&& panel.return_to_queue))
					&& panel.the_shipyard == yard)
			{
				panel.shipyardDetails(yard);
				panel.validate();
				panel.repaint();
			}
		}
	}

}
