public class ShipMoveOrder extends Order
{
	int player_id;
	int ship_id;
	int system_id;
	Destination dest;
	
	public ShipMoveOrder(Player p, Ship s, long t, Destination d)
	{
		player_id = p.getId();
		ship_id = s.getId();
		system_id = s.location.getId();
		scheduled_time=t;
		dest = d;
	}
	
	public void execute(Galaxy g)
	{
		Ship the_ship = g.systems.get(system_id).fleets[player_id].ships.get(ship_id);
		
		//System.out.println(Integer.toString(ship_id) + " orderToMove: t is " + Long.toString(t) + " and time is " + Long.toString(time));
		the_ship.current_flying_AI = new TrackingAI(the_ship, GalacticStrategyConstants.LANDING_RANGE, TrackingAI.MATCH_SPEED);
		the_ship.attacking = false;
	}
	
	public ShipMoveOrder(){}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id = id;}
	public int getShip_id(){return ship_id;}
	public void setShip_id(int id){ship_id = id;}
	public int getSystem_id(){return system_id;}
	public void setSystem_id(int id){system_id=id;}
}