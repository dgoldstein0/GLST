import java.util.Set;
import java.util.HashSet;

public strictfp class ShipPickupTroopsOrder extends Order {

	Ship the_ship;
	
	Describer<Ship> ship_desc;
	
	public ShipPickupTroopsOrder(Player p, Ship s, long t)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		
		ship_desc=s.describer();
		the_ship = s;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
		}
		
		//validate order
		if(the_ship != null && the_ship.owner.getId() == p_id)
		{
			the_ship.update(scheduled_time, null);
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
				else orderDropped();
			}
			else orderDropped();
		}
		else orderDropped();
		
		return new HashSet<Order>();
	}
	
	public ShipPickupTroopsOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
}
