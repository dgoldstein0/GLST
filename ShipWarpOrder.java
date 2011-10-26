
public strictfp class ShipWarpOrder extends Order
{
	Ship the_ship;
	GSystem the_dest;
	
	Describer<Ship> ship_desc;
	Describer<GSystem> dest_desc;
	
	public ShipWarpOrder(Player p, Ship s, long t, GSystem sys)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		the_ship = s;
		ship_desc=s.describer();
		
		the_dest = sys;
		dest_desc=sys.describer();
	}
	
	@Override
	public void execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
			the_dest = dest_desc.retrieveObject(g, scheduled_time);
		}
		
		if(the_ship != null && the_ship.isAliveAt(scheduled_time)
				&& the_ship.owner.getId() == p_id)
		{
			the_ship.orderToWarp(scheduled_time, the_dest);
		}
		else
		{
			orderDropped();
		}
	}
	
	public ShipWarpOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
	public Describer<GSystem> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<GSystem> d){dest_desc=d;}
}