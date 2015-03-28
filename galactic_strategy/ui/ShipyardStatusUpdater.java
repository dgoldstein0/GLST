package galactic_strategy.ui;
import galactic_strategy.game_objects.Shipyard;

import javax.swing.*;

public class ShipyardStatusUpdater extends FacilityStatusUpdater
{
	JProgressBar health;
	JProgressBar manufac_bar;

	Shipyard the_shipyard;
	
	public ShipyardStatusUpdater(JProgressBar hp, JProgressBar m, Shipyard s)
	{
		health = hp;
		manufac_bar = m;
		the_shipyard = s;
	}
	
	public void updateFacility()
	{
		if(the_shipyard.getManufac_queue().size() != 0)
		{
			int percent = (int)(the_shipyard.percentComplete()*100);
			manufac_bar.setValue(percent);
			manufac_bar.setString(Integer.toString(percent)+"%");
		}
		else
		{
			manufac_bar.setValue(0);
			manufac_bar.setString("");
		}
		health.setValue(the_shipyard.getEndurance()-the_shipyard.getDamage());
		health.setString(Integer.toString(the_shipyard.getEndurance()-the_shipyard.getDamage()));
	}
}