package galactic_strategy.user_actions;
import galactic_strategy.Player;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Describer;

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
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_ship = ship_desc.retrieveObject(g);
		
		//validate order
		if(the_ship != null && the_ship.getOwner().getId() == p_id)
		{
			if(the_ship.isAlive() && the_ship.getDestination() instanceof OwnableSatellite<?> &&
					((OwnableSatellite<?>)the_ship.getDestination()).getOwner() == the_ship.getOwner())
			{
				the_ship.orderToPickupTroops(scheduled_time);
				decision = Decision.ACCEPT;
				return true;
			}
		}

		decision = Decision.REJECT;
		return false;
	}
	
	public ShipPickupTroopsOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
}
