import javax.swing.ImageIcon;

public strictfp enum FacilityType
{
	//					name					metal	money	build_time	icon_image_path					tooltip															creator
	NO_BLDG				("",					0,		0,		0l,			"",								"",																null),
	BASE				("Base",				500,	500,	25000l,		"images/Base.gif",				"Recruits troops and protects the planet from invading armies", new FacilityCreator<Base>(){public Base create(OwnableSatellite<?> o, int i, long t){return new Base(o,i,t);}}),
	MINE				("Mine",				0,		200,	20000l,		"images/Mine.gif",				"Extracts metal for use in future manufacturing",				new FacilityCreator<Mine>(){public Mine create(OwnableSatellite<?> o, int i, long t){return new Mine(o,i,t);}}),
	SHIPYARD			("Shipyard",			200,	300,	20000l,		"images/Shipyard.gif",			"Builds spaceships",											new FacilityCreator<Shipyard>(){public Shipyard create(OwnableSatellite<?> o, int i, long t){return new Shipyard(o,i,t);}}),
	RESEARCH_BUILDING	("Research Building",	400,	400,	35000l,		"images/ResearchBuilding.gif",	"Research new technologies to extend your capabilities",		new FacilityCreator<ResearchBuilding>(){public ResearchBuilding create(OwnableSatellite<?> o, int i, long t){return new ResearchBuilding(o,i,t);}});
	
	final String name;
	final int metal_cost;
	final int money_cost;
	final long build_time;
	final ImageIcon icon;
	final FacilityCreator<?> creator;
	final String tooltip;
	
	FacilityType(String nm, int met, int mon, long bt, String icon_path, String tooltip, FacilityCreator<?> fc)
	{
		name=nm;
		metal_cost=met;
		money_cost=mon;
		build_time=bt;
		this.tooltip = tooltip;
		creator = fc;
		
		if(!icon_path.equals(""))
			icon = new ImageIcon(icon_path);
		else
			icon=null;
	}
}