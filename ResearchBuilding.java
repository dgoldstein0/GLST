import java.util.*;

public class ResearchBuilding extends Facility<ResearchBuilding>{
	
	HashSet<ResearchOption> options;
	
	public ResearchBuilding(OwnableSatellite<?> loc, int id, long t)
	{
		super(loc, id, t, GalacticStrategyConstants.initial_research_building_endu);
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
