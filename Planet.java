import java.util.*;

public class Planet extends Satellite
{
	String name;
	long population;
	HashSet<Satellite> satellites;
	HashSet<Facility> facilities;
	
	
	public Planet(String nm, long pop, int sz, double m, byte habitable)
	{
		name=nm;
		population=pop;
		size=sz;
		mass=m;
		habitability=habitable;
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
}