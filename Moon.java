public class Moon extends OwnableSatellite<Moon>
{
	public Moon(int i, double m, String nm, int sz)
	{
		super();
		id=i;
		this.mass=m;
		this.name=nm;
		this.size=sz;
	}
	
	public Moon(){super();}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)((Planet)orbit.boss).orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)((Planet)orbit.boss).orbit.boss).increaseClaim(p);
	}
	
	public String imageLoc(){return "images/moon.jpg";}
}