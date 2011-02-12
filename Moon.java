public strictfp class Moon extends OwnableSatellite<Moon>
{
	public Moon(int i, double m, String nm, int sz)
	{
		super();
		id=i;
		this.mass=m;
		this.name=nm;
		this.size=sz;
		initial_pop = OwnableSatelliteType.Moon.initial_pop;
		pop_capacity = OwnableSatelliteType.Moon.pop_capacity;
		pop_growth_rate = OwnableSatelliteType.Moon.PopGrowthRate;
		building_limit = OwnableSatelliteType.Moon.building_Num;
		mining_r = OwnableSatelliteType.Moon.mining_rate;
		data_control.saveData();
	}
	
	public Moon(){
		super();
		initial_pop=0;
		pop_capacity=0;
		pop_growth_rate=0;
		building_limit=2;
		mining_r=0;
	}
	
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