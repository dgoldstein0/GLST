import java.awt.Color;

public class GalacticStrategyConstants
{
	//this class contains constants relavent to the games/galaxy designer's configuration.
	//It does NOT contain constants that are only of interest within a certain class, such the numeric values which represent different states of 1 variable.
	//For instance, GDFrame has DRAG_NONE, DRAG_DIST, and DRAG_RANGE, which tells the interface if ranges or distances should be drawn during drags.
	//In particular, these values are for the different states of drag_options
	
	//******************************************PHYSICS TO PROGRAM CONVERSIONS*******************************************//
	
	//Used in orbit
	static double PERIOD_CONSTANT =10000; //equiv to 2pi/sqrt(G), where G is the "gravity constant" of the game.
	
	//*******************************DEFAULTS USED IN Galaxy Designer********************************************************//
	
	//Used in SystemViewer
	static int DEFAULT_STAR_SIZE=25;
	static int DEFAULT_STAR_ZONE_SIZE=50;
	static int DEFAULT_PLANET_SIZE = 10;
	static int DEFAULT_MOON_SIZE = 6;
	static double DEFAULT_PLANET_MASS = 10;
	static double DEFAULT_STAR_MASS = 10000;
	static double DEFAULT_MOON_MASS= 1;
	
	static final int MIN_PLANET_SIZE=10;
	static final int MIN_STAR_SIZE=16;
	static final int MIN_MOON_SIZE=6;
	static final int MIN_ASTEROID_SIZE=3;
	static final int MAX_STAR_SIZE=100;
	static final int MAX_PLANET_SIZE=75;
	static final int MAX_MOON_SIZE=30;
	static final int MAX_ASTEROID_SIZE=10;
	
	static final int MAJOR_TICKS_FOR_OBJECT_SIZE = 25;
	static final int MINOR_TICKS_FOR_OBJECT_SIZE = 5;
	
	//Used in GalacticMapPainter and GDFrame
	//these 4 control the distance slider that marks off the ranges to show or the maximum distances that should be drawn
	static final int MAX_DIST=1000;
	static final int DEFAULT_DIST=100; //this is used as the default value for the slider determining the distances shown.  used for max_dist_shown in GalacticMapPainter
	static final int DIST_MINOR_TICKS = 25;
	static final int DIST_MAJOR_TICKS = 100;
	
	//These 4 values set up the Nav level slider and spinner
	static final int MIN_NAV_LEVEL=1;
	static final int MAX_NAV_LEVEL=10;
	static final int DEFAULT_NAV_LEVEL = MAX_NAV_LEVEL;//always start at the max. higher nav level = easier to navigate to.  So unless specified, systems placed are by default easy to get to.
	static final int NAV_LEVEL_TICK_SPACING=1; //navigation slider automatically snaps to ticks.  This SHOULD NOT be changed!!!!!!!  This means nav_level can only take on integer values.  And the nav spinner does the same, using this variable to control how much it can change at a time
	
	//These values set the defaults for the other navigability controls.
	static final int DEFAULT_NAV_OPTIONS = 0; //0=don't show navigabilities, 1=show navigabilities for only selected systems, 2=show all navigabilities
	static final boolean DEFAULT_DISPLAY_UNNAV = false; //false = do not display systems with nav's below the threshold, true = display them, but gray instead of white
	//**********************INTERFACE SETTINGS******************************************************************************//
	
	static final int EDGE_BOUND=40; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	static final int SYS_WIDTH=1200; //the allowed width of a system
	static final int SYS_HEIGHT=1000; //the allowed height of a system
	
	static final int GALAXY_WIDTH=800; //the width of the galaxy
	static final int GALAXY_HEIGHT=600; //the height of the galaxy
	
	static final double DEFAULT_SCALE = 1.0d; //this sets the default scale for the view of the system
	static final double MIN_SCALE = 1.0d;
	static final double MAX_SCALE=5.0d;
	static final double SCROLL_SENSITIVITY=.1d; //the amount the wheel is rotated will be multiplied by this number to determine how much the scale changes.
	
	static final double SELECTION_TOLERANCE = 5.0d; //when the system is clicked on, the program looks for an object centered at the click site within a circle of radius of this many pixels.
	
	//*************************************GAMEPLAY SETTINGS****************************************************************//
	
	static int MAX_PLAYERS = 2;
	
	static Color[] DEFAULT_COLORS = {Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.BLUE};
	
	//Used by Mine
	static int DEFAULT_MINING_RATE=10;
	//Used by Player
	static long DEFAULT_MONEY=1000;
	static long DEFAULT_METAL=1000;
	
	//setup for ship types
	final static int JUNK=0;
	final static ShipType[] sTypes={new ShipType("Junk",20,100,10,200)};
	
	//Used by Shipyard
	static final int queue_capa=10;//the capacity of a shipyard's queue
	
	//Defaults for Bases
	final static int initial_soldier=1000;
	final static int initial_base_endu=500;
	final static int initial_shipyard_endu = 200;
	final static int initial_mine_endu = 150;
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