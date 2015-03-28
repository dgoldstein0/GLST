package galactic_strategy.sync_engine;
import galactic_strategy.Player;
import galactic_strategy.game_objects.Fleet;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.game_objects.ShipId;
import galactic_strategy.game_objects.Shipyard;

public strictfp class ShipDescriber implements Describer<Ship>
{
	int system_id;
	int player_id;
	int q_id;
	FacilityDescriber<Shipyard> manu;
	
	public ShipDescriber(Player p, Ship s)
	{
		player_id = p.getId();
		q_id = s.getId().getQueue_id();
		manu = s.getId().getManufacturer().describer();
		system_id = s.getLocation().getId();
	}
	
	@Override
	public Ship retrieveObject(Galaxy g)
	{
		Fleet fleet = g.getSystems().get(system_id).getFleets()[player_id];
		return fleet.getShips().get(new ShipId(q_id, manu.retrieveObject(g)));
	}
	
	public ShipDescriber(){}
	public int getSystem_id(){return system_id;}
	public void setSystem_id(int i){system_id=i;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int p){player_id=p;}
	public int getQ_id(){return q_id;}
	public void setQ_id(int s){q_id=s;}
	public FacilityDescriber<Shipyard> getManu(){return manu;}
	public void setManu(FacilityDescriber<Shipyard> f){manu=f;}
}