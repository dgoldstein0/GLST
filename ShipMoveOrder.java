public class ShipMoveOrder extends Order
{
	Ship the_ship;
	Destination<?> the_dest;
	
	ShipDescriber ship_desc;
	Describer<? extends Destination<?>> dest_desc;
	
	public ShipMoveOrder(Player p, Ship s, long t, Destination<?> d)
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
			the_ship = ship_desc.retrieveObject(g);
			the_dest= dest_desc.retrieveObject(g);
		}
		
		the_ship.orderToMove(scheduled_time, the_dest);
	}
	
	public ShipMoveOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<? extends Destination<?>> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<? extends Destination<?>> d){dest_desc=d;}
}