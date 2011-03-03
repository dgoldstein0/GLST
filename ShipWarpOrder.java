import java.util.Set;
import java.util.HashSet;

public strictfp class ShipWarpOrder extends Order
{
	Ship the_ship;
	GSystem the_dest;
	int player_id;
	
	Describer<Ship> ship_desc;
	Describer<GSystem> dest_desc;
	
	public ShipWarpOrder(Player p, Ship s, long t, GSystem sys)
	{
		mode = Order.ORIGIN;
		the_ship = s;
		ship_desc=s.describer();
		player_id = p.getId();
		
		the_dest = sys;
		dest_desc=sys.describer();
		scheduled_time=t;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
			the_dest = dest_desc.retrieveObject(g, scheduled_time);
		}
		
		if(the_ship != null && the_ship.isAliveAt(scheduled_time) && the_ship.owner.getId() == player_id)
		{
			the_ship.update(scheduled_time, null);
			Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time);
			the_ship.orderToWarp(scheduled_time, the_dest);
			return orders;
		}
		else
		{
			orderDropped();
			return new HashSet<Order>();
		}
	}
	
	public ShipWarpOrder(){mode=Order.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
	public Describer<GSystem> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<GSystem> d){dest_desc=d;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id=id;}
}