import java.util.*;

public class ResearchBuilding extends Facility<ResearchBuilding>{
	
	public ResearchBuilding(OwnableSatellite<?> loc, int id, long t)
	{
		super(loc, id, t, GalacticStrategyConstants.initial_research_building_endu);
		data_control = new ResearchBuildingDataControl(this);
		data_control.saveData();
	}
	
	public void research(ResearchOption o)
	{
		o.research();
	}
	
	public void updateStatus(long t){}
	
	public String imageLoc(){return "images/researchbldg.gif";}
	public FacilityType getType(){return FacilityType.RESEARCH_BUILDING;}
	public String getName(){return "Research Building";}

	@Override
	public void ownerChanged(long t) {
		// TODO Auto-generated method stub
		
		//probably have a cancel research here
	}
}
