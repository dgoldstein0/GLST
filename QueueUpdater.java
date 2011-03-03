
public class QueueUpdater implements Runnable {

	Shipyard yard;
	
	public QueueUpdater(Shipyard sy)
	{
		yard =sy;
	}
	
	@Override
	public void run()
	{
		System.out.println("update queue");
		PlanetMoonCommandPanel panel = GameInterface.GC.GI.SatellitePanel;
		if(panel != null && yard.location.owner.getId() == GameInterface.GC.player_id
				&& GameInterface.GC.GI.sat_or_ship_disp == GameInterface.PANEL_DISP.SAT_PANEL
				&& panel.the_sat == yard.location
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
