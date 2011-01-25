
public strictfp enum PlanetType {
	
	//Randomness to be implemented later
	//Planet types	Name					PopInit		PopCapacity	Buildings	MiningRate	Description
	SuperPlanet		("Super Planet"			,200		,2000		,7			,.015		,"This planet is fit for a king; Populations will flourish and with lots of metal everywhere."),
	Paradise		("Paradise Planet"		,120		,1000		,5			,.001		,"This paradise contains all the necessary resources to produce a thriving population."),
	MineralRich		("Mountainous Planet"	,60			,300		,5			,.01		,"This planet has an abundance of metals in the ground"),
	Average			("Typical Planet"		,75			,500		,4			,.002		,"This planet is ridiculously average with normal everything"),
	DesertPlanet	("Wasteland Planet"		,10			,100		,2			,0			,"This desert wasteland has little to offer to anybody.");
	
	
	final String namePlanet;
	final int initial_pop;
	final int pop_capacity;
	final int building_Num;
	final double mining_rate;
	final String description;
	
	PlanetType(String nm, int popinit, int popcap, int build, double minerate, String describe){
		namePlanet = nm;
		initial_pop = popinit;
		pop_capacity = popcap;
		building_Num = build;
		mining_rate = minerate;
		description = describe;
	}
	
}
