public class ShipMoveOrder extends Order
{
	Ship the_ship;
	Destination the_dest;
	
	ShipDescriber ship_desc;
	DestDescriber dest_desc;
	
	public ShipMoveOrder(Player p, Ship s, long t, Destination d)
	{
		mode = Order.ORIGIN;
		ship_desc=new ShipDescriber(p,s);
		dest_desc=d.describer();
		scheduled_time=t;
	}
	
	public void execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = (Ship)ship_desc.retrieveDestination(g);
			the_dest=(Destination)dest_desc.retrieveDestination(g);
		}
		
		the_ship.orderToMove(scheduled_time, the_dest);
	}
	
	public ShipMoveOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public DestDescriber getDest_desc(){return dest_desc;}
	public void setDest_desc(DestDescriber d){dest_desc=d;}
}