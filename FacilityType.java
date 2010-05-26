public enum FacilityType
{
	//				name				metal	money	build_time	icon_image_path
	NO_BLDG			("",				0,	0,	0l,		""),
	BASE				("Base",			500,	500,	25000l,	"images/Base.gif"),
	MINE				("Mine",			0,	200,	20000l,	"images/Mine.gif"),
	SHIPYARD			("Shipyard",		200,	300,	20000l,	"images/Shipyard.gif"),
	RESEARCH_BUILDING	("Research Building",	400,	400,	35000l,	"images/ResearchBuilding.gif");
	
	final String name;
	final int metal_cost;
	final int money_cost;
	final long build_time;
	final FacilityIcon icon;
	
	FacilityType(String nm, int met, int mon, long bt, String icon_path)
	{
		name=nm;
		metal_cost=met;
		money_cost=mon;
		build_time=bt;
		
		if(icon_path != "")
			icon = new FacilityIcon(icon_path, this);
		else
			icon=null;
	}
}