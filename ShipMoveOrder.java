public class ShipMoveOrder extends Order
{
	int player_id;
	int ship_id;
	Destination dest;
	
	public ShipMoveOrder(Player p, Ship s, long t, Destination d)
	{
		player_id = p.getId();
		ship_id = s.getId();
		scheduled_time=t;
		dest = d;
	}
	
	
	
	public ShipMoveOrder(){}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id = id;}
	public int getShip_id(){return ship_id;}
	public void setShip_id(int id){ship_id = id;}
}