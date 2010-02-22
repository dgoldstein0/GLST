import java.util.*;

public class Galaxy
{
	HashSet<GSystem> systems;
	String name;
	ArrayList<Planet> start_locations;
	
	public Galaxy()
	{
		systems = new HashSet<GSystem>();
		start_locations = new ArrayList<Planet>();
	}
	
	//methods required for load/save
	public void setSystems(HashSet<GSystem> sys){systems=sys;}
	public HashSet<GSystem> getSystems(){return systems;}
	public void setName(String n){name = n;}
	public String getName(){return name;}
	public void setStart_locations(ArrayList<Planet> loc){start_locations = loc;}
	public ArrayList<Planet> getStart_locations(){return start_locations;}
}