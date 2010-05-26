public class ShipDescriber implements DestDescriber
{
	int system_id;
	int player_id;
	int ship_id;
	
	public ShipDescriber(Player p, Ship s)
	{
		player_id = p.getId();
		ship_id = s.getId();
		system_id = s.location.getId();
	}
	
	public Describable retrieveDestination(Galaxy g)
	{
		return g.systems.get(system_id).fleets[player_id].ships.get(ship_id);
	}
	
	public ShipDescriber(){}
	public int getSystem_id(){return system_id;}
	public void setSystem_id(int i){system_id=i;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int p){player_id=p;}
	public int getShip_id(){return ship_id;}
	public void setShip_id(int s){ship_id=s;}
}