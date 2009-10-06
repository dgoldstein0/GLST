import java.util.*;

public class Galaxy
{
	HashSet<GSystem> systems;
	
	public Galaxy()
	{
		
	}
	
	//methods required for load/save
	public void setSystems(HashSet<GSystem> sys){systems=sys;}
	public HashSet<GSystem> getSystems(){return systems;}
}