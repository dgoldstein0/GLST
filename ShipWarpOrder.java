public class ShipWarpOrder extends Order
{
	Ship the_ship;
	GSystem the_dest;
	
	ShipDescriber ship_desc;
	Describer<GSystem> dest_desc;
	
	public ShipWarpOrder(Player p, Ship s, long t, GSystem sys)
	{
		mode = Order.ORIGIN;
		ship_desc=new ShipDescriber(p,s);
		dest_desc=sys.describer();
		scheduled_time=t;
	}
	
	public void execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g);
			the_dest = dest_desc.retrieveObject(g);
		}
		
		the_ship.orderToWarp(scheduled_time, the_dest);
	}
	
	public ShipWarpOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<GSystem> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<GSystem> d){dest_desc=d;}
}