
public class ShipyardBuildShipOrder extends Order {

	FacilityDescriber shipyard_describer;
	Shipyard the_yard;
	ShipType type;
	
	public ShipyardBuildShipOrder(Shipyard s, ShipType tp, long t)
	{
		the_yard=s;
		shipyard_describer = (FacilityDescriber) s.describer();
		type=tp;
		scheduled_time=t;
		mode = Order.ORIGIN;
	}
	
	@Override
	public void execute(Galaxy g) {
		if(mode==NETWORK)
			the_yard = (Shipyard)shipyard_describer.retrieveDestination(g);
		
		the_yard.addToQueue(new Ship(type), scheduled_time);
	}

	public ShipyardBuildShipOrder(){mode=Order.NETWORK;}
	public FacilityDescriber getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber desc){shipyard_describer=desc;}
	public ShipType getType(){return type;}
	public void setType(ShipType t){type=t;}
}
