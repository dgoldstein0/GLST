import java.util.Set;
import java.util.HashSet;

public class ShipyardCancelBuildOrder extends Order {

	FacilityDescriber<Shipyard> shipyard_describer;
	int ship_id;
	int player_id;
	
	private Shipyard the_yard;
	private Ship the_ship;
	
	
	public ShipyardCancelBuildOrder(Shipyard syd, Ship s, long t)
	{
		the_yard=syd;
		shipyard_describer = syd.describer();
		scheduled_time=t;
		ship_id = s.getId().queue_id;
		the_ship = s;
		player_id = syd.location.owner.getId();
		
		mode = Order.ORIGIN;
	}
	
	@Override
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		if(mode==NETWORK)
			the_yard = (Shipyard)shipyard_describer.retrieveObject(g, scheduled_time);
		
		if(the_yard != null)
		{
			ShipyardDataSaver data = (ShipyardDataSaver) the_yard.data_control.saved_data[the_yard.data_control.getIndexForTime(scheduled_time)];
			OwnableSatelliteDataSaverControl<?> ctrl = the_yard.location.data_control;
			
			if(data.alive && data.queue.size() != 0 && ctrl.saved_data[ctrl.getIndexForTime(scheduled_time)].own.getId() == player_id) //validity check: is the Shipyard still alive?
			{
				Set<Order> orders = the_yard.data_control.revertToTime(scheduled_time);
				
				if (mode == NETWORK)
					the_ship = the_yard.manufac_queue.get(ship_id);
				
				if(the_ship != null)
				{
					orders.addAll(the_ship.data_control.revertToTime(scheduled_time));
					orders.addAll(ctrl.revertToTime(scheduled_time)); //make sure it uses the right owner
					
					the_yard.removeFromQueue(the_ship, scheduled_time, false);
				}
				else
					orderDropped();
				
				return orders;
			}
			else
				orderDropped();
		}
		else
			orderDropped();
		
		return new HashSet<Order>();
	}

	public ShipyardCancelBuildOrder(){mode=Order.NETWORK;}
	
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public int getShip_id(){return ship_id;}
	public void setShip_id(int i){ship_id=i;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id=id;}
}
