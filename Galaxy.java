import java.util.*;

public class Galaxy
{
	HashSet<GSystem> systems;
	String name;
	Planet[] start_locations;
	
	public Galaxy()
	{
		
	}
	
	//methods required for load/save
	public void setSystems(HashSet<GSystem> sys){systems=sys;}
	public HashSet<GSystem> getSystems(){return systems;}
	public void setName(String n){name = n;}
	public String getName(){return name;}
	public void setStart_locations(Planet[] loc){start_locations = loc;}
	public Planet[] getStart_locations(){return start_locations;}
}