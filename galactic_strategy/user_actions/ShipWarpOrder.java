package galactic_strategy.user_actions;
import galactic_strategy.Player;
import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Describer;

public strictfp class ShipWarpOrder extends Order
{
	Ship the_ship;
	GSystem the_dest;
	
	Describer<Ship> ship_desc;
	Describer<GSystem> dest_desc;
	
	public ShipWarpOrder(Player p, Ship s, long t, GSystem sys)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		the_ship = s;
		ship_desc=s.describer();
		
		the_dest = sys;
		dest_desc=sys.describer();
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_ship = ship_desc.retrieveObject(g);
		the_dest = dest_desc.retrieveObject(g);
		
		if(the_ship != null && the_ship.isAlive()
				&& the_ship.getOwner().getId() == p_id)
		{
			the_ship.orderToWarp(the_dest);
			decision = Decision.ACCEPT;
			return true;
		}
		else
		{
			decision = Decision.REJECT;
			return false;
		}
	}
	
	public ShipWarpOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
	public Describer<GSystem> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<GSystem> d){dest_desc=d;}
}