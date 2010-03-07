import java.util.*;

public class Moon extends OwnableSatellite
{
	public Moon(double m, String nm, int sz)
	{
		this.mass=m;
		this.name=nm;
		this.size=sz;
		facilities = new HashSet<Facility>();
	}
	
	public Moon(){facilities = new HashSet<Facility>();}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)((Planet)orbit.boss).orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)((Planet)orbit.boss).orbit.boss).increaseClaim(p);
	}
}