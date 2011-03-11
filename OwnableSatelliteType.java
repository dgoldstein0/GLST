
public strictfp enum OwnableSatelliteType {

	//Randomness to be implemented later
	//Planet types	Name					PopInit		PopGrowthRate		PopCapacity	Buildings	MiningRate	Description
	Void			(""						,0			,0.0				,0			,0			,0			,""),
	Moon			("Moon"					,0			,0.0				,0			,3			,0			,"It's a moon"),
	SuperPlanet		("Super Planet"			,200		,.000015				,2000		,7			,.006		,"This planet is fit for a king; Populations will flourish and with lots of metal everywhere."),
	Paradise		("Paradise Planet"		,120		,.000015				,1000		,5			,.0015		,"This paradise contains all the necessary resources to produce a thriving population."),
	MineralRich		("Mountainous Planet"	,60			,.000015				,300		,5			,.007		,"This planet has an abundance of metals in the ground"),
	Average			("Typical Planet"		,100		,.000015				,3000		,7			,.0025		,"This planet is ridiculously average with normal everything"),
	DesertPlanet	("Wasteland Planet"		,10			,.000015				,100		,3			,.001		,"This desert wasteland has little to offer to anybody.");
	
	
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
