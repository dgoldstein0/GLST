import java.util.*;

public class Moon extends Satellite
{
	String name;
	HashSet<Facility> facilities;
	Player owner;
	
	public Moon(double m, String name, int sz)
	{
		this.mass=m;
		this.name=name;
		this.size=sz;
	}
	
	public Moon(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public HashSet<Facility> getFacilities(){return facilities;}
	public void setFacilities(HashSet<Facility> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner=p;}
}