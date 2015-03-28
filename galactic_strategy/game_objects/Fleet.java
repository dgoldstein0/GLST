package galactic_strategy.game_objects;
import galactic_strategy.Player;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Saveable;

import java.util.TreeMap;
import java.util.Iterator;

public strictfp class Fleet implements Saveable<Fleet>
{
	TreeMap<ShipId, Ship> ships;
	Player owner;
	GSystem location;
	
	DataSaverControl<Fleet> data_control;
	
	public Fleet(GSystem loc, Player o)
	{
		ships = new TreeMap<ShipId, Ship>();
		location = loc;
		owner = o;
		data_control = new DataSaverControl<Fleet>(this);
		data_control.saveData(0);
	}
	
	//methods required for load/save
	public Fleet(){}
	public TreeMap<ShipId, Ship> getShips(){return ships;}
	public void setShips(TreeMap<ShipId, Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	public void setLocation(GSystem sys){location=sys;}
	public GSystem getLocation(){return location;}
	
	public void update(long time)
	{
		synchronized(this)
		{
			Fleet.ShipIterator ship_iteration = iterator();
			for(ShipId j; ship_iteration.hasNext();)
			{
				j=ship_iteration.next();
				Ship s = ships.get(j);
				s.update(time, ship_iteration);
			}
		}
	}
	
	public void add(Ship s)
	{
		synchronized(this)
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
		synchronized(this)
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
	
	public class ShipIterator implements Iterator<ShipId>
	{
		final Iterator<ShipId> the_iterator;
		
		public ShipIterator(Iterator<ShipId> iterator) {
			the_iterator = iterator;
		}
		
		@Override
		public ShipId next()
		{
			return the_iterator.next();
		}
		
		@Override
		public boolean hasNext()
		{
			return the_iterator.hasNext();
		}

		public void remove() {
			synchronized(this)
			{
				the_iterator.remove();
				location.decreaseClaim(owner);
			}
		}
	}
}