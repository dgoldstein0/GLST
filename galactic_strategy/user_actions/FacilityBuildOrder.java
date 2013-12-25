package galactic_strategy.user_actions;
import galactic_strategy.game_objects.FacilityType;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.SatelliteDescriber;
import galactic_strategy.ui.GameInterface;


public strictfp class FacilityBuildOrder extends Order
{
	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	FacilityType bldg_type;
	
	public FacilityBuildOrder(OwnableSatellite<?> sat, FacilityType btype, long t)
	{
		super(t, sat.getOwner());
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		bldg_type=btype;
		mode = Order.MODE.ORIGIN;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_sat = sat_desc.retrieveObject(g);
		boolean is_building = false;
		
		//validate - check if owner is the same as orderer at the time the order should be executed
		if(the_sat.getOwner() == GameInterface.GC.getPlayers()[p_id])
		{
			is_building = the_sat.scheduleConstruction(bldg_type, scheduled_time);
		}
		
		if (is_building)
		{
			decision = Decision.ACCEPT;
			return true;
		}
		else
		{
			decision = Decision.REJECT;
			return false;
		}
	}
	
	public FacilityBuildOrder(){mode = Order.MODE.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
	public FacilityType getBldg_type(){return bldg_type;}
	public void setBldg_type(FacilityType b){bldg_type=b;}
}