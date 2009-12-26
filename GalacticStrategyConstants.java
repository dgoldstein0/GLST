public class GalacticStrategyConstants
{
	//this class contains constants relavent to the games/galaxy designer's configuration.
	//It does NOT contain constants that are only of interest within a certain class, such the numeric values which represent different states of 1 variable.
	//For instance, GDFrame has DRAG_NONE, DRAG_DIST, and DRAG_RANGE, which tells the interface if ranges or distances should be drawn during drags.
	//In particular, these values are for the different states of drag_options
	
	//******************************************PHYSICS TO PROGRAM CONVERSIONS*******************************************//
	
	//Used in orbit
	static double PERIOD_CONSTANT =1000; //equiv to 2pi/sqrt(G), where G is the "gravity constant" of the game.
	
	//*******************************DEFAULTS USED IN Galaxy Designer********************************************************//
	
	//Used in SystemViewer
	static int DEFAULT_STAR_SIZE=25;
	static int DEFAULT_STAR_ZONE_SIZE=50;
	static int DEFAULT_PLANET_SIZE = 10;
	static int DEFAULT_MOON_SIZE = 6;
	static double DEFAULT_PLANET_MASS = 10;
	static double DEFAULT_STAR_MASS = 10000;
	static double DEFAULT_MOON_MASS= 1;
	
	//Used in GalacticMapPainter
	static final int DEFAULT_MAX_DIST_SHOWN=100;
	static final int DEFAULT_NAV_LEVEL = 10;
	
	//**********************INTERFACE SETTINGS******************************************************************************//
	
	static int EDGE_BOUND=40; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static int SYS_WIDTH=1200; //the allowed width of a system
	static int SYS_HEIGHT=1000; //the allowed height of a system
	
	//*************************************GAMEPLAY SETTINGS****************************************************************//
	
	//Used by Mine
	static int DEFAULT_MINING_RATE=10;
	//Used by Player
	static long DEFAULT_MONEY=100;
	
	//setup for ship types
	final static int JUNK=0;
	final static ShipType[] sTypes={new ShipType("Junk",20,100,10,200)};
	
	//Used by Shipyard
	static final int queue_capa=10;//the capacity of a shipyard's queue
	
	//Defaults for Bases
	final static int initial_soldier=1000;
	final static int initial_endu=100;
	final static int solider_upgraderate=1000;
	final static int endu_upgraderate=100;
	
	//***************************************************************GENERAL DATA*******************************************//
	
	//used in Star
	//these integers correspond to the indices of the corresponding image url in the array color_choice
	final static int COLOR_NULL=0;
	final static int COLOR_RED=1;
	final static int COLOR_ORANGE=2;
	final static int COLOR_YELLOW=3;
	final static int COLOR_WHITE=4;
	final static int COLOR_BLUE=5;
	
	//stores image URLs
	final static String[] color_choice={"images/null.png","images/red.png","images/orange.png","images/yellow.png","images/white.png","images/blue.png"};
	
	//used by GameControl for networking
	static final int DEFAULT_PORT_NUMBER = 7007;
	
}