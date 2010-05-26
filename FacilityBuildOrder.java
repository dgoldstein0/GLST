public class FacilityBuildOrder extends Order
{
	SatelliteDescriber sat_desc;
	OwnableSatellite the_sat;
	FacilityType bldg_type;
	
	public FacilityBuildOrder(OwnableSatellite sat, FacilityType btype, long t)
	{
		the_sat=sat;
		sat_desc = (SatelliteDescriber)sat.describer();
		bldg_type=btype;
		scheduled_time=t;
		mode = Order.ORIGIN;
	}
	
	public void execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
			the_sat = (OwnableSatellite)sat_desc.retrieveDestination(g);
		
		the_sat.scheduleConstruction(bldg_type, scheduled_time);
	}
	
	public FacilityBuildOrder(){mode = Order.NETWORK;}
	public SatelliteDescriber getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber s){sat_desc=s;}
	public FacilityType getBldg_type(){return bldg_type;}
	public void setBldg_type(FacilityType b){bldg_type=b;}
}