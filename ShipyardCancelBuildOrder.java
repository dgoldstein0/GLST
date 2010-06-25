
public class ShipyardCancelBuildOrder extends Order {

	FacilityDescriber<Shipyard> shipyard_describer;
	int ship_id;
	
	Shipyard the_yard;
	Ship the_ship;
	
	
	public ShipyardCancelBuildOrder(Shipyard syd, Ship s, long t)
	{
		the_yard=syd;
		shipyard_describer = syd.describer();
		scheduled_time=t;
		ship_id = s.getId();
		the_ship = s;
		mode = Order.ORIGIN;
	}
	
	@Override
	public void execute(Galaxy g) {
		if(mode==NETWORK)
		{
			the_yard = (Shipyard)shipyard_describer.retrieveObject(g);
			the_ship = the_yard.manufac_queue.get(ship_id);
		}
		
		the_yard.removeFromQueue(the_ship, scheduled_time);
	}

	public ShipyardCancelBuildOrder(){mode=Order.NETWORK;}
	
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public int getShip_id(){return ship_id;}
	public void setType(int i){ship_id=i;}
}
