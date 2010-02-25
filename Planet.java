import java.util.*;

public class Planet extends Satellite
{
	long population;
	HashSet<Satellite> satellites;
	HashSet<Facility> facilities;
	Player owner;
	
	public Planet(String nm, long pop, int sz, double m, byte habitable)
	{
		name=nm;
		population=pop;
		size=sz;
		mass=m;
		habitability=habitable;
		satellites=new HashSet<Satellite>();
		facilities=new HashSet<Facility>();
	}
	
	public void popChange()
	{
		
	}
	
	//methods required for load/save
	public Planet(){facilities = new HashSet<Facility>();}
	public HashSet<Satellite> getSatellites(){return satellites;}
	public void setSatellites(HashSet<Satellite> sat){satellites=sat;}
	public HashSet<Facility> getFacilities(){return facilities;}
	public void setFacilities(HashSet<Facility> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	
	public void setOwner(Player p)
	{
		if(owner instanceof Player)
			((GSystem)orbit.boss).decreaseClaim(owner);
		owner=p;
		((GSystem)orbit.boss).increaseClaim(p);
	}
}