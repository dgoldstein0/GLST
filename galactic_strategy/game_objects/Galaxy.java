package galactic_strategy.game_objects;

import galactic_strategy.Player;
import galactic_strategy.sync_engine.DataSaverControl;

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

	public void saveAllData(Player[] players, long time)
	{
		for (Player p : players)
		{
			if (p != null)
			{
				p.data_control.saveData(time);
				for (Ship s : p.getShips_in_transit())
				{
					s.data_control.saveData(time);
					s.flying_part.data_control.saveData(time);
				}
			}
		}
		
		for (GSystem sys : systems)
		{
			sys.saveOwnablesData(time);
			for (Fleet f : sys.fleets)
			{
				f.data_control.saveData(time);
				for (ShipId id : f.ships.keySet())
				{
					Ship s = f.ships.get(id);
					s.data_control.saveData(time);
					s.flying_part.data_control.saveData(time);
				}
			}
			
			sys.missiles.data_control.saveData(time);
			for (Missile.MissileId id : sys.missiles.table.keySet())
			{
				Missile m = sys.missiles.table.get(id);
				m.data_control.saveData(time);
				m.flying_part.data_control.saveData(time);
			}
		}
	}
	
	public void revertAllToTime(long t, Player[] players) throws DataSaverControl.DataNotYetSavedException
	{
		for (Player p : players)
		{
			if (p != null)
			{
				p.data_control.revertToTime(t);
				for (Ship s : p.getShips_in_transit())
				{
					s.data_control.revertToTime(t);
					s.flying_part.data_control.revertToTime(t);
				}
			}
		}
		
		for (GSystem sys : systems)
		{
			sys.revertOwnables(t);
			for (Fleet f : sys.fleets)
			{
				f.data_control.revertToTime(t);
				for (ShipId id : f.ships.keySet())
				{
					Ship s = f.ships.get(id);
					s.data_control.revertToTime(t);
					s.flying_part.data_control.revertToTime(t);
				}
			}
			
			sys.missiles.data_control.revertToTime(t);
			for (Missile.MissileId id : sys.missiles.table.keySet())
			{
				Missile m = sys.missiles.table.get(id);
				m.data_control.revertToTime(t);
				m.flying_part.data_control.revertToTime(t);
			}
			
			sys.recalculateClaims();
		}
	}
	
	//TODO: what is this for?
	public void saveOwnablesData(long time) {
		
		for(GSystem s : systems)
		{
			s.saveOwnablesData(time);
		}
	}
}