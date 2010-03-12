import java.util.*;

public class Galaxy
{
	ArrayList<GSystem> systems;
	String name;
	ArrayList<Planet> start_locations;
	
	public Galaxy()
	{
		systems = new ArrayList<GSystem>();
		start_locations = new ArrayList<Planet>();
	}
	
	//methods required for load/save
	public void setSystems(ArrayList<GSystem> sys){systems=sys;}
	public ArrayList<GSystem> getSystems(){return systems;}
	public void setName(String n){name = n;}
	public String getName(){return name;}
	public void setStart_locations(ArrayList<Planet> loc){start_locations = loc;}
	public ArrayList<Planet> getStart_locations(){return start_locations;}
}