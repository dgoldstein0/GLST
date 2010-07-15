import java.util.Set;
import java.util.HashSet;

public class ShipInvadeOrder extends Order {

	Ship the_ship;
	ShipDescriber ship_desc;
	
	public ShipInvadeOrder(Player p, Ship s, long t)
	{
		mode = Order.ORIGIN;
		ship_desc=new ShipDescriber(p,s);
		scheduled_time=t;
	}
	
	@Override
	public Set<Order> execute(Galaxy g) {
		
		if(mode == Order.NETWORK)
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
		
		//validate order
		if(the_ship != null)
		{
			ShipDataSaver data = (ShipDataSaver)the_ship.data_control.saved_data[the_ship.data_control.getIndexForTime(scheduled_time)];
			if(data.is_alive && data.dest instanceof OwnableSatellite<?>)
			{
				Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time);
				orders.addAll(((OwnableSatellite<?>)the_ship.destination).getDataControl().revertToTime(scheduled_time));
				
				the_ship.orderToInvade((OwnableSatellite<?>)the_ship.destination,scheduled_time);
				
				return orders;
			}
		}
		
		return new HashSet<Order>();
	}

	public ShipInvadeOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
}
