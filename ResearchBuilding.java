import java.util.*;

public strictfp class ResearchBuilding extends Facility<ResearchBuilding>{
	
	public ResearchBuilding(OwnableSatellite<?> loc, int id, long t)
	{
		super(loc, id, t, GalacticStrategyConstants.initial_research_building_endu);
	}
	
	public void research(ResearchOption o)
	{
		o.research();
	}
	
	public void updateStatus(long t){}
	
	public FacilityType getType(){return FacilityType.RESEARCH_BUILDING;}

	@Override
	public void ownerChanged(long t) {
		// TODO Auto-generated method stub
		
		//probably have a cancel research here
	}
}
