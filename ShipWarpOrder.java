public class ShipWarpOrder extends Order
{
	Ship the_ship;
	GSystem the_dest;
	
	ShipDescriber ship_desc;
	DestDescriber dest_desc;
	
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
			the_ship = (Ship)ship_desc.retrieveDestination(g);
			the_dest=(GSystem)dest_desc.retrieveDestination(g);
		}
		
		the_ship.orderToWarp(scheduled_time, the_dest);
	}
	
	public ShipWarpOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public DestDescriber getDest_desc(){return dest_desc;}
	public void setDest_desc(DestDescriber d){dest_desc=d;}
}