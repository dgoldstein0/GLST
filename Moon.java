public class Moon extends OwnableSatellite<Moon>
{
	public Moon(int i, double m, String nm, int sz)
	{
		super();
		id=i;
		this.mass=m;
		this.name=nm;
		this.size=sz;
		
		data_control.saveData();
	}
	
	public Moon(){super();}
	
	@Override
	public void setOwner(Player p)
	{
		if(owner != null)
			((GSystem)((Planet)orbit.boss).orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)((Planet)orbit.boss).orbit.boss).increaseClaim(p);
	}
	
	@Override
	public String imageLoc(){return "images/moon.jpg";}

	@Override
	public void recursiveSaveData() {
		data_control.saveData();
	}
}