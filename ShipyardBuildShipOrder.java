public strictfp class ShipyardBuildShipOrder extends Order
{
	FacilityDescriber<Shipyard> shipyard_describer;
	Shipyard the_yard;
	ShipType type;
	
	public ShipyardBuildShipOrder(Shipyard s, ShipType tp, long t)
	{
		super(t, s.location.owner);
		
		the_yard=s;
		shipyard_describer = s.describer();
		type=tp;
		scheduled_time=t;
		mode = Order.MODE.ORIGIN;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==MODE.NETWORK)
			the_yard = shipyard_describer.retrieveObject(g, scheduled_time);
		
		if(the_yard != null)
		{	
			OwnableSatelliteDataSaverControl<?> ctrl = the_yard.location.data_control;
			if(GameInterface.GC.players[p_id] == ctrl.saved_data[ctrl.getIndexForTime(scheduled_time)].own)
			{
				the_yard.addToQueue(new Ship(type), scheduled_time);
				decision = Decision.ACCEPT;
				return true;
			}
		}
		
		decision = Decision.REJECT;
		return false;
	}

	public ShipyardBuildShipOrder(){mode=Order.MODE.NETWORK;}
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public ShipType getType(){return type;}
	public void setType(ShipType t){type=t;}
}
