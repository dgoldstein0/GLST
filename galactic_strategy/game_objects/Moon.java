package galactic_strategy.game_objects;

public strictfp class Moon extends OwnableSatellite<Moon>
{
	public Moon(int i, double m, String nm, int sz)
	{
		super();
		id=i;
		this.mass=m;
		this.name=nm;
		this.size=sz;
		init_defaults();
	}
	
	public Moon() {
		init_defaults();
	}
	
	private void init_defaults() {
		initial_pop = OwnableSatelliteType.Moon.initial_pop;
		pop_capacity = OwnableSatelliteType.Moon.pop_capacity;
		pop_growth_rate = OwnableSatelliteType.Moon.PopGrowthRate;
		building_limit = OwnableSatelliteType.Moon.building_Num;
		base_mining_r = OwnableSatelliteType.Moon.mining_rate;
		current_mining_r = OwnableSatelliteType.Moon.mining_rate;
	}
	
	@Override
	public GSystem getGSystem()
	{
		return ((Planet)orbit.boss).getGSystem();
	}
	
	@Override
	public String imageLoc(){return "images/moon.jpg";}
}