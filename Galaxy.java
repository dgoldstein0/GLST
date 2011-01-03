import java.util.*;

public strictfp class Galaxy
{
	ArrayList<GSystem> systems;
	String name;
	ArrayList<OwnableSatellite<?>> start_locations;
	
	public Galaxy()
	{
		systems = new ArrayList<GSystem>();
		start_locations = new ArrayList<OwnableSatellite<?>>();
	}
	
	//methods required for load/save
	public void setSystems(ArrayList<GSystem> sys){systems=sys;}
	public ArrayList<GSystem> getSystems(){return systems;}
	public void setName(String n){name = n;}
	public String getName(){return name;}
	public void setStart_locations(ArrayList<OwnableSatellite<?>> loc){start_locations = loc;}
	public ArrayList<OwnableSatellite<?>> getStart_locations(){return start_locations;}

	public void saveOwnablesData() {
		
		for(GSystem s : systems)
		{
			s.saveOwnablesData();
		}
	}
}