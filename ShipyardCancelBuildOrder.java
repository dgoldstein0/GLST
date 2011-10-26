
public strictfp class ShipyardCancelBuildOrder extends Order {

	FacilityDescriber<Shipyard> shipyard_describer;
	int ship_id;
	
	private Shipyard the_yard;
	private Ship the_ship;
	
	public ShipyardCancelBuildOrder(Shipyard syd, Ship s, long t)
	{
		super(t, syd.location.owner);
		
		the_yard=syd;
		shipyard_describer = syd.describer();
		scheduled_time=t;
		ship_id = s.getId().queue_id;
		the_ship = s;
		
		mode = Order.MODE.ORIGIN;
	}
	
	@Override
	public void execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		if(mode==MODE.NETWORK)
			the_yard = (Shipyard)shipyard_describer.retrieveObject(g, scheduled_time);
		
		if(the_yard != null)
		{
			ShipyardDataSaver data = (ShipyardDataSaver) the_yard.data_control.saved_data[the_yard.data_control.getIndexForTime(scheduled_time)];
			OwnableSatelliteDataSaverControl<?> ctrl = the_yard.location.data_control;
			
			if(data.alive && data.queue.size() != 0 &&
					ctrl.saved_data[ctrl.getIndexForTime(scheduled_time)].own.getId() == p_id) //validity check: is the Shipyard still alive?
			{
				
				if (mode == MODE.NETWORK)
					the_ship = the_yard.manufac_queue.get(ship_id);
				
				if(the_ship != null)
				{
					the_yard.removeFromQueue(the_ship, scheduled_time);
				}
				else
					orderDropped();
			}
			else
				orderDropped();
		}
		else
			orderDropped();
	}

	public ShipyardCancelBuildOrder(){mode=Order.MODE.NETWORK;}
	
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public int getShip_id(){return ship_id;}
	public void setShip_id(int i){ship_id=i;}
}
