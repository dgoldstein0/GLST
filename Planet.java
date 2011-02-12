import java.util.*;

public strictfp class Planet extends OwnableSatellite<Planet>
{
	public Planet(int i, String nm, double init_pop, double pop_cap, int sz, double m, double growth_rate, int building_Num, double mining_rate)
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
		mining_r = mining_rate;
		
		size=sz;
		mass=m;
		
		orbiting=new ArrayList<Satellite<?>>();
		facilities=new HashMap<Integer, Facility<?>>();
		data_control.saveData();
	}
	
	//methods required for load/save
	public Planet(){
		super();
		initial_pop = 100;
		pop_capacity = 10000;
		pop_growth_rate = .000005;
		building_limit = 5;
		mining_r = .02;
	}
	
	@Override
	public GSystem getGSystem()
	{
		return (GSystem)orbit.boss;
	}
	
	public String imageLoc(){return "images/planet.jpg";}

	@Override
	public void recursiveSaveData() {
		data_control.saveData();
		for(Satellite<?> sat : orbiting)
		{
			sat.recursiveSaveData();
		}
	}
}