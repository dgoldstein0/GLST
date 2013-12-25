package galactic_strategy.user_actions;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.game_objects.ShipType;
import galactic_strategy.game_objects.Shipyard;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.FacilityDescriber;
import galactic_strategy.ui.GameInterface;

public strictfp class ShipyardBuildShipOrder extends Order
{
	FacilityDescriber<Shipyard> shipyard_describer;
	Shipyard the_yard;
	ShipType type;
	
	public ShipyardBuildShipOrder(Shipyard s, ShipType tp, long t)
	{
		super(t, s.getOwner());
		
		the_yard=s;
		shipyard_describer = s.describer();
		type=tp;
		scheduled_time=t;
		mode = Order.MODE.ORIGIN;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_yard = shipyard_describer.retrieveObject(g);
		
		if(the_yard != null)
		{
			if(GameInterface.GC.getPlayers()[p_id] == the_yard.getOwner())
			{
				the_yard.addToQueue(new Ship(type), scheduled_time);
				decision = Decision.ACCEPT;
				return true;
			}
		}
		
		decision = Decision.REJECT;
		return false;
	}

	public ShipyardBuildShipOrder(){mode=Order.MODE.NETWORK;}
	public FacilityDescriber<Shipyard> getShipyard_describer(){return shipyard_describer;}
	public void setShipyard_describer(FacilityDescriber<Shipyard> desc){shipyard_describer=desc;}
	public ShipType getType(){return type;}
	public void setType(ShipType t){type=t;}
}
