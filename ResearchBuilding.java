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
	
	public void updateStatus(long t){}
	
	public String imageLoc(){return "images/researchbldg.gif";}
	public int getType(){return Facility.RESEARCH_BUILDING;}
	public String getName(){return "Research Building";}
}
