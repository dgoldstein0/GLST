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
	
	public void remove(Ship s)
	{
		ships.remove(s.getId());
		location.decreaseClaim(owner);
	}
}