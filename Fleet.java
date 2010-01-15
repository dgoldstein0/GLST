import java.util.HashSet;

public class Fleet
{
	String name;
	HashSet<Ship> ships;
	Player owner;
	
	public Fleet()
	{
		ships = new HashSet<Ship>();
	}
	
	//methods required for load/save
	public HashSet<Ship> getShips(){return ships;}
	public void setShips(HashSet<Ship> sh){ships=sh;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	
	public void add(Ship s)
	{
		ships.add(s);
	}
	
	public void remove(Ship s)
	{
		ships.remove(s);
	}
}