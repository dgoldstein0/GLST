import java.util.*;

public class Planet extends OwnableSatellite
{
	HashSet<Satellite> satellites;
	
	public Planet(String nm, double init_pop, double pop_cap, int sz, double m, double growth_rate)
	{
		name=nm;
		
		//set up population numbers
		initial_pop=init_pop;
		pop_capacity = pop_cap;
		population=(long)init_pop;
		pop_growth_rate = growth_rate;
		
		size=sz;
		mass=m;
		
		satellites=new HashSet<Satellite>();
		facilities=new HashSet<Facility>();
	}
	
	//methods required for load/save
	public Planet(){
		facilities = new HashSet<Facility>();
		initial_pop = 100;
		pop_capacity = 10000;
		pop_growth_rate = .000005;
	}
	public HashSet<Satellite> getSatellites(){return satellites;}
	public void setSatellites(HashSet<Satellite> sat){satellites=sat;}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)orbit.boss).increaseClaim(p);
	}
}