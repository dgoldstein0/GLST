import java.util.*;

public class ResearchBuilding extends Facility{
	
	HashSet<ResearchOption> options;
	
	public ResearchBuilding(OwnableSatellite loc, long t)
	{
		super(loc, t, GalacticStrategyConstants.initial_research_building_endu);
		options=new HashSet<ResearchOption>();
	}
	
	public void research(ResearchOption o)
	{
		o.research();
	}
	
	public void updateStatus(long t){}
	
	public String imageLoc(){return "images/researchbldg.gif";}
	public FacilityType getType(){return FacilityType.RESEARCH_BUILDING;}
	public String getName(){return "Research Building";}
}
