import java.util.*;

public class ResearchBuilding extends Facility{
	
	HashSet<ResearchOption> options;
	
	public ResearchBuilding()
	{
		options=new HashSet<ResearchOption>();
	}
	
	public void research(ResearchOption o)
	{
		o.research();
	}
}
