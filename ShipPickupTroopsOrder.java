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
		the_ship = ship_desc.retrieveObject(g);
		
		//validate order
		if(the_ship != null && the_ship.owner.getId() == p_id)
		{
			if(the_ship.is_alive && the_ship.destination instanceof OwnableSatellite<?> && ((OwnableSatellite<?>)the_ship.destination).owner == the_ship.owner)
			{
				the_ship.orderToPickupTroops(scheduled_time);
				decision = Decision.ACCEPT;
				return true;
			}
		}

		decision = Decision.REJECT;
		return false;
	}
	
	public ShipPickupTroopsOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
}
