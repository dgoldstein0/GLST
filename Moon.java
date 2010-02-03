import java.util.*;

public class Moon extends Satellite
{
	HashSet<Facility> facilities;
	Player owner;
	
	public Moon(double m, String nm, int sz)
	{
		this.mass=m;
		this.name=nm;
		this.size=sz;
	}
	
	public Moon(){}
	public HashSet<Facility> getFacilities(){return facilities;}
	public void setFacilities(HashSet<Facility> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner=p;}
}