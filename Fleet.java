import java.util.TreeMap;
import java.util.Iterator;

public strictfp class Fleet implements Saveable<Fleet>
{
	TreeMap<Ship.ShipId, Ship> ships;
	Player owner;
	GSystem location;
	Object lock = new Object();
	
	DataSaverControl<Fleet> data_control;
	
	public Fleet(GSystem loc, Player o)
	{
		ships = new TreeMap<Ship.ShipId, Ship>();
		location = loc;
		owner = o;
		data_control = new DataSaverControl<Fleet>(this);
		data_control.saveData(0);
	}
	
	//methods required for load/save
	public Fleet(){}
	public TreeMap<Ship.ShipId, Ship> getShips(){return ships;}
	public void setShips(TreeMap<Ship.ShipId, Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	public void setLocation(GSystem sys){location=sys;}
	public GSystem getLocation(){return location;}
	
	public void update(long time)
	{
		synchronized(lock)
		{
			Fleet.ShipIterator ship_iteration = iterator();
			for(Ship.ShipId j; ship_iteration.hasNext();)
			{
				j=ship_iteration.next();
				Ship s = ships.get(j);
				s.update(time, ship_iteration);
			}
		}
	}
	
	public void add(Ship s)
	{
		synchronized(lock)
		{
			System.out.println("Adding ship to fleet: queue_id = " + s.getId().queue_id);
			System.out.println("\tmanufacturer has id " + s.getId().manufacturer.getId());

			Ship val = ships.put(s.getId(), s);
			System.out.println("\tval is null: " + Boolean.toString(val==null));
		}
		location.increaseClaim(owner);
	}
	
	public boolean remove(Ship s)
	{
		//this removes the ship.  remove() returns the ship if the ship was in the hashtable, so
		//check against null makes sure we are not removing a ship that has already been removed
		//before decreasing the claim
		synchronized(lock)
		{
			if(ships.remove(s.getId()) != null)
			{
				location.decreaseClaim(owner);
				return true;
			}
		}
		
		return false;
	}
	
	public ShipIterator iterator()
	{
		return new ShipIterator(ships.keySet().iterator());
	}

	@Override
	public DataSaverControl<Fleet> getDataControl() {
		
		return data_control;
	}

	@Override
	public void handleDataNotSaved() {
		
		throw new RuntimeException("handleDataNotSaved called for Fleet!  This is impossible!");
	}
	
	public class ShipIterator implements Iterator<Ship.ShipId>
	{
		final Iterator<Ship.ShipId> the_iterator;
		
		public ShipIterator(Iterator<Ship.ShipId> iterator) {
			the_iterator = iterator;
		}
		
		@Override
		public Ship.ShipId next()
		{
			return the_iterator.next();
		}
		
		@Override
		public boolean hasNext()
		{
			return the_iterator.hasNext();
		}

		public void remove() {
			synchronized(lock)
			{
				the_iterator.remove();
				location.decreaseClaim(owner);
			}
		}
	}
}