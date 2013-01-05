
public class PlanetMoonCommandPanelUpdater implements Runnable {

	OwnableSatellite<?> sat;
	
	public PlanetMoonCommandPanelUpdater(OwnableSatellite<?> sys)
	{
		sat =sys;
	}
	
	@Override
	public void run() {
			PlanetMoonCommandPanel panel = GameInterface.GC.GI.SatellitePanel;
			if(panel != null && GameInterface.GC.GI.sat_or_ship_disp == GameInterface.PANEL_DISP.SAT_PANEL
					&& panel.the_sat == sat)
			{
				panel.setSat(sat);
			}
	}

}
