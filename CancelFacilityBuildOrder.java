
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
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_sat = sat_desc.retrieveObject(g, scheduled_time);
		}
		
		//verify that the player ordering this is the owner of the planet.
		if(the_sat.owner.getId() == p_id)
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
