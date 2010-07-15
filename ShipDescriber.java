public class ShipDescriber implements Describer<Ship>
{
	int system_id;
	int player_id;
	int q_id;
	FacilityDescriber<Shipyard> manu;
	
	public ShipDescriber(Player p, Ship s)
	{
		player_id = p.getId();
		q_id = s.getId().queue_id;
		manu = s.getId().manufacturer.describer();
		system_id = s.location.getId();
	}
	
	@Override
	public Ship retrieveObject(Galaxy g, long t)
	{
		FleetDataSaverControl fleet_ctrl = g.systems.get(system_id).fleets[player_id].data_control;
		return fleet_ctrl.saved_data[fleet_ctrl.getIndexForTime(t)].ships.get(new Ship.ShipId(q_id, manu.retrieveObject(g, t)));
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