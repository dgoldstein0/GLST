import java.util.Set;
import java.util.HashSet;

public class ShipPickupTroopsOrder extends Order {

	Ship the_ship;
	
	ShipDescriber ship_desc;
	
	public ShipPickupTroopsOrder(Player p, Ship s, long t)
	{
		mode = Order.ORIGIN;
		ship_desc=new ShipDescriber(p,s);
		scheduled_time=t;
	}
	
	public Set<Order> execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
		}
		
		//validate order
		if(the_ship != null)
		{
			ShipDataSaver data = (ShipDataSaver) the_ship.data_control.saved_data[the_ship.data_control.getIndexForTime(scheduled_time)];
			if(data.is_alive && ((OwnableSatellite<?>)data.dest).owner == the_ship.owner && data.dest instanceof OwnableSatellite<?>)
			{
				Base b = ((OwnableSatellite<?>)data.dest).data_control.saved_data[((OwnableSatellite<?>)data.dest).data_control.getIndexForTime(scheduled_time)].base;
				if(b != null)
				{
				
					Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time); /*only this ship is affected,
							since the order to pickup troops does not interrupt any other orders or
							the flight pattern, since the ship will move the same whether it is
							picking up troops or not.  POTENTIALLY can ignore reverting other objects... but lets not*/
					
					orders.addAll(b.data_control.revertToTime(scheduled_time));
								
					the_ship.orderToPickupTroops(scheduled_time);
					
					return orders;
				}
			}
		}
		
		return new HashSet<Order>();
	}
	
	public ShipPickupTroopsOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
}
