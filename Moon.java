import java.util.*;

public class Moon extends OwnableSatellite
{
	public Moon(int i, double m, String nm, int sz)
	{
		id=i;
		this.mass=m;
		this.name=nm;
		this.size=sz;
		facilities = new ArrayList<Facility>();
	}
	
	public Moon(){facilities = new ArrayList<Facility>();}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)((Planet)orbit.boss).orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)((Planet)orbit.boss).orbit.boss).increaseClaim(p);
	}
}