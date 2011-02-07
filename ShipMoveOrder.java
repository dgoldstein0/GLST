import java.util.Set;
import java.util.HashSet;

public strictfp class ShipMoveOrder extends Order
{
	Ship the_ship;
	Destination<?> the_dest;
	
	Describer<Ship> ship_desc;
	Describer<? extends Destination<?>> dest_desc;
	
	int player_id;
	
	public ShipMoveOrder(Player p, Ship s, long t, Destination<?> d)
	{
		mode = Order.ORIGIN;
		
		the_ship = s;
		ship_desc=s.describer();
		
		the_dest = d;
		dest_desc=d.describer();
		
		player_id = p.getId();
		
		scheduled_time=t;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
			the_dest = dest_desc.retrieveObject(g, scheduled_time);
		}
		
		/*System.out.println("ship move order executing with scheduled_time = " + Long.toString(scheduled_time));
		System.out.println("\tthe_ship is null: " + Boolean.toString(the_ship == null));
		System.out.println("\tthe_dest is null: " + Boolean.toString(the_dest==null));
		if(the_ship != null)
		{
			System.out.println("\tthe_ship is alive at scheduled_time: " + Boolean.toString(the_ship.isAliveAt(scheduled_time)));
			System.out.println("\tthe_ship.owner.getId() = " + Integer.toString(the_ship.owner.getId()) + " and player_id = " + Integer.toString(player_id));
		}*/
		
		if(the_ship != null && the_dest != null && the_ship.isAliveAt(scheduled_time) && the_ship.owner.getId() == player_id)
		{
			//System.out.println("\trevert and execute...");
			
			the_ship.update(scheduled_time, null);
			Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time);
			
			/*TODO: should we revert destination?  this implementation assumes setting an object as a
			 * destination has no effect on that object.  As of 7/13/10, this is true.*/
			
			if(the_dest instanceof Flyer<?,?,?>)
				((Flyer<?,?,?>)the_dest).update(scheduled_time,null);
			
			the_ship.orderToMove(scheduled_time, the_dest);
			
			return orders;
		}
		else
		{
			orderDropped();
			return new HashSet<Order>();
		}
	}
	
	public ShipMoveOrder(){mode=Order.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
	public Describer<? extends Destination<?>> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<? extends Destination<?>> d){dest_desc=d;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id = id;}

	@Override
	public void doInstantly(Galaxy g) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}