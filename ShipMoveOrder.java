import java.util.Set;
import java.util.HashSet;

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
	
	public Set<Order> execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
			the_dest = dest_desc.retrieveObject(g, scheduled_time);
		}
		
		if(the_ship != null && the_dest != null && the_ship.isAliveAt(scheduled_time))
		{
			Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time);
			
			/*TODO: should we revert destination?  this implementation assumes setting an object as a
			 * destination has no effect on that object.  As of 7/13/10, this is true.*/
			
			the_ship.orderToMove(scheduled_time, the_dest);
			
			return orders;
		}
		else
			return new HashSet<Order>();
	}
	
	public ShipMoveOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<? extends Destination<?>> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<? extends Destination<?>> d){dest_desc=d;}
}