import java.util.*;

public class Planet extends OwnableSatellite<Planet>
{
	public Planet(int i, String nm, double init_pop, double pop_cap, int sz, double m, double growth_rate)
	{
		super();
		name=nm;
		id=i;
		
		//set up population numbers
		initial_pop=init_pop;
		pop_capacity = pop_cap;
		population=(long)init_pop;
		pop_growth_rate = growth_rate;
		
		size=sz;
		mass=m;
		
		orbiting=new ArrayList<Satellite<?>>();
		facilities=new Hashtable<Integer, Facility<?>>();
		data_control.saveData();
	}
	
	//methods required for load/save
	public Planet(){
		super();
		initial_pop = 100;
		pop_capacity = 10000;
		pop_growth_rate = .000005;
	}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)orbit.boss).increaseClaim(p);
	}
	
	public String imageLoc(){return "images/planet.jpg";}
}