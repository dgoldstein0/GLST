package galactic_strategy.user_actions;

import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.SatelliteDescriber;

public strictfp class CancelFacilityBuildOrder extends Order {

	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	
	public CancelFacilityBuildOrder(OwnableSatellite<?> sat, long t)
	{
		super(t, sat.getOwner());
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		mode = Order.MODE.ORIGIN;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_sat = sat_desc.retrieveObject(g);
		
		//verify that the player ordering this is the owner of the planet.
		if(the_sat.getOwner().getId() == p_id)
		{	
			//the actual execution of the order.
			the_sat.cancelConstruction(scheduled_time);
			decision = Decision.ACCEPT;
			return true;
		}
		else
		{
			decision = Decision.REJECT;
			return false;
		}
	}
	
	public CancelFacilityBuildOrder(){mode = Order.MODE.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
}
