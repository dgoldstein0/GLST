
public strictfp enum OwnableSatelliteType {

	//Randomness to be implemented later
	//Planet types	Name					PopInit		PopGrowthRate		PopCapacity	Buildings	MiningRate	Description
	Void			(""						,0			,0.0				,0			,0			,0			,""),
	Moon			("Moon"					,0			,0.0				,0			,2			,0			,"It's a moon"),
	SuperPlanet		("Super Planet"			,200		,.00002				,2000		,7			,.015		,"This planet is fit for a king; Populations will flourish and with lots of metal everywhere."),
	Paradise		("Paradise Planet"		,120		,.00002				,1000		,5			,.001		,"This paradise contains all the necessary resources to produce a thriving population."),
	MineralRich		("Mountainous Planet"	,60			,.00002				,300		,5			,.01		,"This planet has an abundance of metals in the ground"),
	Average			("Typical Planet"		,75			,.00002				,500		,4			,.002		,"This planet is ridiculously average with normal everything"),
	DesertPlanet	("Wasteland Planet"		,10			,.00002				,100		,3			,.005		,"This desert wasteland has little to offer to anybody.");
	
	
	final String namePlanet;
	final int initial_pop;
	final double PopGrowthRate;
	final int pop_capacity;
	final int building_Num;
	final double mining_rate;
	final String description;
	
	OwnableSatelliteType(String nm, int popinit, double popgrowthrate, int popcap, int build, double minerate, String describe){
		namePlanet = nm;
		initial_pop = popinit;
		PopGrowthRate = popgrowthrate;
		pop_capacity = popcap;
		building_Num = build;
		mining_rate = minerate;
		description = describe;
	}
	
}
