package galactic_strategy.ui;

import galactic_strategy.game_objects.OwnableSatellite;

public class PlanetMoonCommandPanelUpdater implements Runnable {

	OwnableSatellite<?> sat;
	
	public PlanetMoonCommandPanelUpdater(OwnableSatellite<?> sys)
	{
		sat =sys;
	}
	
	@Override
	public void run() {
			PlanetMoonCommandPanel panel = GameInterface.GC.GI.SatellitePanel;
			if(panel != null && GameInterface.GC.GI.displayed_control_panel == GameInterface.PANEL_DISP.SAT_PANEL
					&& panel.the_sat == sat)
			{
				panel.setSat(sat);
			}
	}

}
