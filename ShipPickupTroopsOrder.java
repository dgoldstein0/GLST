public strictfp class ShipPickupTroopsOrder extends Order {

	Ship the_ship;
	
	Describer<Ship> ship_desc;
	
	public ShipPickupTroopsOrder(Player p, Ship s, long t)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		
		ship_desc=s.describer();
		the_ship = s;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
		}
		
		//validate order
		if(the_ship != null && the_ship.owner.getId() == p_id)
		{
			ShipDataSaver data = (ShipDataSaver) the_ship.data_control.saved_data[the_ship.data_control.getIndexForTime(scheduled_time)];
			if(data.is_alive && ((OwnableSatellite<?>)data.dest).owner == the_ship.owner && data.dest instanceof OwnableSatellite<?>)
			{
				Base b = ((OwnableSatellite<?>)data.dest).data_control.saved_data[((OwnableSatellite<?>)data.dest).data_control.getIndexForTime(scheduled_time)].base;
				if(b != null)
				{					
					the_ship.orderToPickupTroops(scheduled_time);
					decision = Decision.ACCEPT;
					return true;
				}
			}
		}

		decision = Decision.REJECT;
		return false;
	}
	
	public ShipPickupTroopsOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
}
