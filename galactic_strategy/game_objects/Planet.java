package galactic_strategy.game_objects;

import java.util.ArrayList;
import java.util.HashMap;

public strictfp class Planet extends OwnableSatellite<Planet>
{
	public Planet(int i, String nm, double init_pop, double pop_cap, int sz, double m,
	              double growth_rate, int building_Num, double mining_rate)
	{
		super();
		name=nm;
		id=i;
		
		//set up population numbers
		initial_pop=init_pop;
		pop_capacity = pop_cap;
		population=(long)init_pop;
		pop_growth_rate = growth_rate;
		building_limit = building_Num;
		base_mining_r = mining_rate;
		current_mining_r=mining_rate;
		
		size=sz;
		mass=m;
		
		orbiting=new ArrayList<Satellite<?>>();
		facilities=new HashMap<Integer, Facility<?>>();
	}
	
	public Planet() {init_defaults();}
	
	private void init_defaults() {
		initial_pop = OwnableSatelliteType.Average.initial_pop;
		pop_capacity = OwnableSatelliteType.Average.pop_capacity;
		pop_growth_rate = OwnableSatelliteType.Average.PopGrowthRate;
		building_limit = OwnableSatelliteType.Average.building_Num;
		base_mining_r = OwnableSatelliteType.Average.mining_rate;
		current_mining_r = OwnableSatelliteType.Average.mining_rate;
	}
	
	@Override
	public GSystem getGSystem()
	{
		return (GSystem)orbit.boss;
	}
		
	public String imageLoc(){return "images/planet.jpg";}
}
