import javax.swing.ImageIcon;

public enum FacilityType
{
	//					name					metal	money	build_time	icon_image_path					creator
	NO_BLDG				("",					0,		0,		0l,			"",								null),
	BASE				("Base",				500,	500,	25000l,		"images/Base.gif",				new FacilityCreator<Base>(){public Base create(OwnableSatellite<?> o, int i, long t){return new Base(o,i,t);}}),
	MINE				("Mine",				0,		200,	20000l,		"images/Mine.gif",				new FacilityCreator<Mine>(){public Mine create(OwnableSatellite<?> o, int i, long t){return new Mine(o,i,t);}}),
	SHIPYARD			("Shipyard",			200,	300,	20000l,		"images/Shipyard.gif",			new FacilityCreator<Shipyard>(){public Shipyard create(OwnableSatellite<?> o, int i, long t){return new Shipyard(o,i,t);}}),
	RESEARCH_BUILDING	("Research Building",	400,	400,	35000l,		"images/ResearchBuilding.gif",	new FacilityCreator<ResearchBuilding>(){public ResearchBuilding create(OwnableSatellite<?> o, int i, long t){return new ResearchBuilding(o,i,t);}});
	
	final String name;
	final int metal_cost;
	final int money_cost;
	final long build_time;
	final ImageIcon icon;
	final FacilityCreator<?> creator;
	
	FacilityType(String nm, int met, int mon, long bt, String icon_path, FacilityCreator<?> fc)
	{
		name=nm;
		metal_cost=met;
		money_cost=mon;
		build_time=bt;
		creator = fc;
		
		if(icon_path != "")
			icon = new ImageIcon(icon_path);
		else
			icon=null;
	}
}