public class FacilityBuildOrder extends Order
{
	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	FacilityType bldg_type;
	
	public FacilityBuildOrder(OwnableSatellite<?> sat, FacilityType btype, long t)
	{
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		bldg_type=btype;
		scheduled_time=t;
		mode = Order.ORIGIN;
	}
	
	public void execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
			the_sat = sat_desc.retrieveObject(g);
		
		the_sat.scheduleConstruction(bldg_type, scheduled_time);
	}
	
	public FacilityBuildOrder(){mode = Order.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
	public FacilityType getBldg_type(){return bldg_type;}
	public void setBldg_type(FacilityType b){bldg_type=b;}
}