import java.util.HashMap;

public class Fleet implements RelaxedSaveable<Fleet>
{
	HashMap<Ship.ShipId, Ship> ships;
	Player owner;
	GSystem location;
	Object lock = new Object();
	
	FleetDataSaverControl data_control;
	long last_time_changed;
	
	public Fleet(GSystem loc, Player o)
	{
		ships = new HashMap<Ship.ShipId, Ship>();
		location = loc;
		owner = o;
		data_control = new FleetDataSaverControl(this);
		last_time_changed=0;
		data_control.saveData();
	}
	
	//methods required for load/save
	public HashMap<Ship.ShipId, Ship> getShips(){return ships;}
	public void setShips(HashMap<Ship.ShipId, Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	public void setLocation(GSystem sys){location=sys;}
	public GSystem getLocation(){return location;}
	
	public void add(Ship s, long t)
	{
		synchronized(lock)
		{
			System.out.println("Adding ship to fleet: queue_id = " + s.getId().queue_id);
			System.out.println("\tmanufacturer has id " + s.getId().manufacturer.getId());
			System.out.println("\tAt time " + Long.toString(t));
			
			Ship val = ships.put(s.getId(), s);
			System.out.println("\tval is null: " + Boolean.toString(val==null));
			last_time_changed=t;
			data_control.saveData();
		}
		location.increaseClaim(owner);
	}
	
	public boolean remove(Ship s, long t)
	{
		//this removes the ship.  remove() returns the ship if the ship was in the hashtable, so
		//check against null makes sure we are not removing a ship that has already been removed
		//before decreasing the claim
		synchronized(lock)
		{
			if(ships.remove(s.getId()) != null)
			{
				last_time_changed = t;
				location.decreaseClaim(owner);
				data_control.saveData();
				return true;
			}
		}
		
		return false;
	}

	@Override
	public FleetDataSaverControl getDataControl() {
		
		return data_control;
	}

	@Override
	public void handleDataNotSaved(long t) {
		
		System.out.println("handleDataNotSaved called for Fleet!  This is impossible!");
	}

	@Override
	public long getTime() {
		return last_time_changed;
	}

	@Override
	public void setTime(long t) {
		last_time_changed = t;
	}
}