import java.util.ArrayList;

public strictfp class Galaxy
{
	ArrayList<GSystem> systems;
	String name;
	ArrayList<OwnableSatellite<?>> start_locations;
	
	public Galaxy()
	{
		systems = new ArrayList<GSystem>();
		start_locations = new ArrayList<OwnableSatellite<?>>();
	}
	
	//methods required for load/save
	public void setSystems(ArrayList<GSystem> sys){systems=sys;}
	public ArrayList<GSystem> getSystems(){return systems;}
	public void setName(String n){name = n;}
	public String getName(){return name;}
	public void setStart_locations(ArrayList<OwnableSatellite<?>> loc){start_locations = loc;}
	public ArrayList<OwnableSatellite<?>> getStart_locations(){return start_locations;}

	public void saveAllData(Player[] players)
	{
		for (Player p : players)
		{
			if (p != null)
			{
				p.data_control.saveData();
				for (Ship s : p.ships_in_transit)
				{
					s.data_control.saveData();
				}
			}
		}
		
		for (GSystem sys : systems)
		{
			sys.saveOwnablesData();
			for (Fleet f : sys.fleets)
			{
				f.data_control.saveData();
				for (Ship.ShipId id : f.ships.keySet())
				{
					Ship s = f.ships.get(id);
					s.data_control.saveData();
				}
			}
			
			sys.missiles.data_control.saveData();
			for (Missile.MissileId id : sys.missiles.table.keySet())
			{
				Missile m = sys.missiles.table.get(id);
				m.data_control.saveData();
			}
		}
	}
	
	public void revertAllToTime(long t, Player[] players) throws DataSaverControl.DataNotYetSavedException
	{
		invalidateAllCaches();
		
		for (Player p : players)
		{
			if (p != null)
			{
				p.data_control.revertToTime(t);
				for (Ship s : p.ships_in_transit)
				{
					s.data_control.revertToTime(t);
				}
			}
		}
		
		for (GSystem sys : systems)
		{
			sys.revertOwnables(t);
			for (Fleet f : sys.fleets)
			{
				f.data_control.revertToTime(t);
				for (Ship.ShipId id : f.ships.keySet())
				{
					Ship s = f.ships.get(id);
					s.data_control.revertToTime(t);
				}
			}
			
			sys.missiles.data_control.revertToTime(t);
			for (Missile.MissileId id : sys.missiles.table.keySet())
			{
				Missile m = sys.missiles.table.get(id);
				m.data_control.revertToTime(t);
			}
			
			sys.recalculateClaims();
		}
	}
	
	public void invalidateAllCaches()
	{
		for (GSystem sys : systems)
		{
			for (Fleet f : sys.fleets)
			{
				for (Ship.ShipId id : f.ships.keySet())
				{
					Ship s = f.ships.get(id);
					s.invalidateCache();
				}
			}
			
			for (Missile.MissileId id : sys.missiles.table.keySet())
			{
				Missile m = sys.missiles.table.get(id);
				m.invalidateCache();
			}
		}
	}
	
	//TODO: what is this for?
	public void saveOwnablesData() {
		
		for(GSystem s : systems)
		{
			s.saveOwnablesData();
		}
	}
}