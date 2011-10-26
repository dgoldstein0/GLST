
public strictfp class CancelFacilityBuildOrder extends Order {

	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	
	public CancelFacilityBuildOrder(OwnableSatellite<?> sat, long t)
	{
		super(t, sat.owner);
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		mode = Order.MODE.ORIGIN;
	}
	
	public void execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_sat = sat_desc.retrieveObject(g, scheduled_time);
		}
		
		if(the_sat.owner.getId() == p_id) //verify that the player ordering this is the owner of the planet.
		{	
			//the actual execution of the order.
			the_sat.cancelConstruction(scheduled_time); //(bldg_type, scheduled_time);
		}
		else
			orderDropped();
	}
	
	public CancelFacilityBuildOrder(){mode = Order.MODE.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
}
