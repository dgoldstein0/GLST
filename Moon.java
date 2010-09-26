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
	public GSystem getGSystem()
	{
		return ((Planet)orbit.boss).getGSystem();
	}
	
	@Override
	public String imageLoc(){return "images/moon.jpg";}

	@Override
	public void recursiveSaveData() {
		data_control.saveData();
	}
}