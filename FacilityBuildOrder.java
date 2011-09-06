import java.util.Set;
import java.util.HashSet;

public strictfp class FacilityBuildOrder extends Order
{
	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	FacilityType bldg_type;
	
	public FacilityBuildOrder(OwnableSatellite<?> sat, FacilityType btype, long t)
	{
		super(t, sat.owner);
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		bldg_type=btype;
		mode = Order.MODE.ORIGIN;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_sat = sat_desc.retrieveObject(g, scheduled_time);
		}
		
		//validate - check if owner is the same as orderer at the time the order should be executed
		if(the_sat.data_control.saved_data[the_sat.data_control.getIndexForTime(scheduled_time)].own == GameInterface.GC.players[p_id])
		{
			Set<Order> need_to_reexecute = the_sat.data_control.revertToTime(scheduled_time); //TODO: will revert everything associated with the planet (via facilities).  necessary?		
			
			the_sat.scheduleConstruction(bldg_type, scheduled_time);
			
			return need_to_reexecute;
		}
		else
		{
			orderDropped();
			return new HashSet<Order>();
		}
	}
	
	public FacilityBuildOrder(){mode = Order.MODE.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
	public FacilityType getBldg_type(){return bldg_type;}
	public void setBldg_type(FacilityType b){bldg_type=b;}
}