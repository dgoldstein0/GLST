import java.util.HashSet;

public class Fleet
{
	HashSet<Ship> ships;
	Player owner;
	GSystem location;
	
	public Fleet(GSystem loc, Player o)
	{
		ships = new HashSet<Ship>();
		location = loc;
		owner = o;
	}
	
	//methods required for load/save
	public HashSet<Ship> getShips(){return ships;}
	public void setShips(HashSet<Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	public void setLocation(GSystem sys){location=sys;}
	public GSystem getLocation(){return location;}
	
	public void add(Ship s)
	{
		ships.add(s);
		location.increaseClaim(owner);
	}
	
	public void remove(Ship s)
	{
		ships.remove(s);
		location.decreaseClaim(owner);
	}
}