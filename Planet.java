import java.util.*;

public class Planet extends Satellite
{
	long population;
	HashSet<Satellite> satellites;
	HashSet<Facility> facilities;
	Player owner;
	GSystem system;
	
	public Planet(GSystem sys, String nm, long pop, int sz, double m, byte habitable)
	{
		system=sys;
		name=nm;
		population=pop;
		size=sz;
		mass=m;
		habitability=habitable;
		satellites=new HashSet<Satellite>();
	}
	
	public void popChange()
	{
		
	}
	
	//methods required for load/save
	public Planet(){}
	public HashSet<Satellite> getSatellites(){return satellites;}
	public void setSatellites(HashSet<Satellite> sat){satellites=sat;}
	public HashSet<Facility> getFacilities(){return facilities;}
	public void setFacilities(HashSet<Facility> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner=p;}
}