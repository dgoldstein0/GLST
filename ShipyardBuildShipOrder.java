import java.util.Set;
import java.util.HashSet;

public strictfp class ShipyardBuildShipOrder extends Order {

	FacilityDescriber<Shipyard> shipyard_describer;
	Shipyard the_yard;
	ShipType type;
	int player_id;
	
	public ShipyardBuildShipOrder(Shipyard s, ShipType tp, long t)
	{
		the_yard=s;
		shipyard_describer = s.describer();
		type=tp;
		scheduled_time=t;
		mode = Order.ORIGIN;
		player_id = s.location.owner.getId();
	}
	
	@Override
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		if(mode==NETWORK)
			the_yard = shipyard_describer.retrieveObject(g, scheduled_time);
		
		if(the_yard != null)
		{	
			OwnableSatelliteDataSaverControl<?> ctrl = the_yard.location.data_control;
			if(GameInterface.GC.players[player_id] == ctrl.saved_data[ctrl.getIndexForTime(scheduled_time)].own)
			{
				Set<Order> orders = the_yard.data_control.revertToTime(scheduled_time);
				orders.addAll(the_yard.location.data_control.revertToTime(scheduled_time)); //make sure it uses the right owner
				the_yard.addToQueue(new Ship(type), scheduled_time, false);
				return orders;
			}
			else orderDropped();
		}
		else orderDropped();
		
		return new HashSet<Order>();
	}

	public ShipyardBuildShipOrder(){mode=Order.NETWORK;}
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public ShipType getType(){return type;}
	public void setType(ShipType t){type=t;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int pid){player_id=pid;}

	@Override
	public void doInstantly(Galaxy g) {
		
		the_yard = shipyard_describer.retrieveObject(g, scheduled_time);
		
		the_yard.addToQueue(new Ship(type), scheduled_time, true);
	}
}
