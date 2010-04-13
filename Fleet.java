import java.util.Hashtable;

public class Fleet
{
	Hashtable<Integer, Ship> ships;
	Player owner;
	GSystem location;
	
	public Fleet(GSystem loc, Player o)
	{
		ships = new Hashtable<Integer, Ship>();
		location = loc;
		owner = o;
	}
	
	//methods required for load/save
	public Hashtable<Integer, Ship> getShips(){return ships;}
	public void setShips(Hashtable<Integer, Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	public void setLocation(GSystem sys){location=sys;}
	public GSystem getLocation(){return location;}
	
	public void add(Ship s)
	{
		ships.put(s.getId(), s);
		location.increaseClaim(owner);
	}
	
	public boolean remove(Ship s)
	{
		//this removes the ship.  remove() returns the ship if the ship was in the hashtable, so
		//the instanceof makes sure we are not removing a ship that has already been removed before decreasing the claim
		Boolean remove_successful = (ships.remove(s.getId()) instanceof Ship);
		if(remove_successful)
			location.decreaseClaim(owner);
		
		return remove_successful;
	}
}