package galactic_strategy.user_actions;
import galactic_strategy.Player;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.ShipDescriber;

public strictfp class ShipInvadeOrder extends Order
{

	Ship the_ship;
	ShipDescriber ship_desc;
	
	public ShipInvadeOrder(Player p, Ship s, long t)
	{
		super(t, p);
		
		mode = Order.MODE.ORIGIN;
		the_ship = s;
		ship_desc=new ShipDescriber(p,s);
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		
		the_ship = ship_desc.retrieveObject(g);
		
		//validate order
		if(the_ship != null)
		{
			if(the_ship.isAlive() && the_ship.getDestination() instanceof OwnableSatellite<?>
				&& the_ship.getOwner().getId() == p_id)
			{				
				the_ship.orderToInvade((OwnableSatellite<?>)the_ship.getDestination(),scheduled_time);
				decision = Decision.ACCEPT;
				return true;
			}
			else
			{
				decision = Decision.REJECT;
				return false;
			}
		}
		else
		{
			decision = Decision.REJECT;
			return false;
		}
	}

	public ShipInvadeOrder(){mode=Order.MODE.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public int getPlayer_id(){return p_id;}
	public void setPlayer_id(int i){p_id = i;}
}
