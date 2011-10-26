import java.util.ArrayList;

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

	public void saveAllData()
	{
		for (GSystem sys : systems)
		{
			sys.saveOwnablesData();
			for (Fleet f : sys.fleets)
			{
				f.data_control.saveData();
			}
			
			sys.missiles.data_control.saveData();
		}
	}
	
	public void revertAllToTime(long t) throws DataSaverControl.DataNotYetSavedException
	{
		for (GSystem sys : systems)
		{
			sys.revertOwnables(t);
			for (Fleet f : sys.fleets)
			{
				f.data_control.revertToTime(t);
			}
			
			sys.missiles.data_control.revertToTime(t);
		}
	}
	
	//TODO: what is this for?
	public void saveOwnablesData() {
		
		for(GSystem s : systems)
		{
			s.saveOwnablesData();
		}
	}
}