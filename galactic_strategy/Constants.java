package galactic_strategy;

import java.awt.Color;

public strictfp class Constants
{
	//this class contains constants relevant to the games/galaxy designer's configuration.
	//It does NOT contain constants that are only of interest within a certain class, such the numeric values which represent different states of 1 variable.
	//For instance, GDFrame has DRAG_NONE, DRAG_DIST, and DRAG_RANGE, which tells the interface if ranges or distances should be drawn during drags.
	//In particular, these values are for the different states of drag_options
	
	//time_granularity is the amount of time, in ms, between updates of the game.  Used as a time increment in ship physics.
	public final static long TIME_GRANULARITY = 20; //20ms -> 50 fps
	public final static long TIME_BETWEEN_RESOURCES = 3000; //you get resources every 3 seconds.
	public final static int data_capacity=150; //amount of time grains to save data from.  150 grains at 20ms per grain = 3 seconds
	
	//******************************************PHYSICS TO PROGRAM CONVERSIONS*******************************************//
	
	//Used in orbit
	public static double PERIOD_CONSTANT =100000; //equiv to 2pi/sqrt(G), where G is the "gravity constant" of the game.
	
	//*******************************DEFAULTS USED IN Galaxy Designer********************************************************//
	
	//Used in SystemViewer
	public static int DEFAULT_STAR_SIZE=35;
	public static int DEFAULT_STAR_ZONE_SIZE=50;
	public static int DEFAULT_PLANET_SIZE = 20;
	public static int DEFAULT_MOON_SIZE = 12;
	public static double DEFAULT_PLANET_MASS = 10;
	public static double DEFAULT_STAR_MASS = 10000;
	public static double DEFAULT_MOON_MASS= 1;
	
	public static final int MIN_PLANET_SIZE=10;
	public static final int MIN_STAR_SIZE=16;
	public static final int MIN_MOON_SIZE=6;
	public static final int MIN_ASTEROID_SIZE=3;
	public static final int MAX_STAR_SIZE=100;
	public static final int MAX_PLANET_SIZE=75;
	public static final int MAX_MOON_SIZE=30;
	public static final int MAX_ASTEROID_SIZE=10;
	
	public static final int MAJOR_TICKS_FOR_OBJECT_SIZE = 25;
	public static final int MINOR_TICKS_FOR_OBJECT_SIZE = 5;
	
	//Used in GalacticMapPainter and GDFrame
	//these 4 control the distance slider that marks off the ranges to show or the maximum distances that should be drawn
	public static final int MAX_DIST=1000;
	public static final int DEFAULT_DIST=100; //this is used as the default value for the slider determining the distances shown.  used for max_dist_shown in GalacticMapPainter
	public static final int DIST_MINOR_TICKS = 25;
	public static final int DIST_MAJOR_TICKS = 100;
	
	//These 4 values set up the Nav level slider and spinner
	public static final int MIN_NAV_LEVEL=1;
	public static final int MAX_NAV_LEVEL=10;
	public static final int DEFAULT_NAV_LEVEL = MAX_NAV_LEVEL;//always start at the max. higher nav level = easier to navigate to.  So unless specified, systems placed are by default easy to get to.
	public static final int NAV_LEVEL_TICK_SPACING=1; //navigation slider automatically snaps to ticks.  This SHOULD NOT be changed!!!!!!!  This means nav_level can only take on integer values.  And the nav spinner does the same, using this variable to control how much it can change at a time
	
	//These values set the defaults for the other navigability controls.
	public static final int DEFAULT_NAV_OPTIONS = 0; //0=don't show navigabilities, 1=show navigabilities for only selected systems, 2=show all navigabilities
	public static final boolean DEFAULT_DISPLAY_UNNAV = false; //false = do not display systems with nav's below the threshold, true = display them, but gray instead of white
	//**********************INTERFACE SETTINGS******************************************************************************//
	
	public static final int EDGE_BOUND=40; //this is the distance from the edge of the system, in pixels, at which the system will start to be scrolled
	public static final int SYS_WIDTH=1200; //the allowed width of a system
	public static final int SYS_HEIGHT=1000; //the allowed height of a system
	
	public static final int GALAXY_WIDTH=800; //the width of the galaxy
	public static final int GALAXY_HEIGHT=600; //the height of the galaxy
	
	public static final double DEFAULT_SCALE = 1.0d; //this sets the default scale for the view of the system
	public static final double MIN_SCALE = 0.66d;
	public static final double MAX_SCALE=5.0d;
	public static final double SCROLL_SENSITIVITY=.1d; //the amount the wheel is rotated will be multiplied by this number to determine how much the scale changes.
	
	public static final double SELECTION_TOLERANCE = 5.0d; //when the system is clicked on, the program looks for an object centered at the click site within a circle of radius of this many pixels.
	
	//*************************************GAMEPLAY SETTINGS****************************************************************//
	
	public static int MAX_PLAYERS = 2;
	
	public static Color[] DEFAULT_COLORS = {Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.BLUE};
	
	//default build times and costs for facilities configurable in FacilityType
	
	public final static double LANDING_RANGE = 10.0d; //the max distance at which ships can get/send troops to planets
	public final static float troop_transfer_rate = .1f;
	//Used by TaxOffice
	public static double DEFAULT_INCOME_RATE = .01; //$ per tax update per person
	//Used by Player
	public static long DEFAULT_MONEY=1000;
	public static long DEFAULT_METAL=1000;
	public static boolean fogofwar = false;
	
	//ship attacking range
	public static final double Detection_Range_Sq = 40000.0;
	public static final double Attacking_Range_Sq=40000.0;
	public static final double Attacking_Range=200.0;
	public static final long Attacking_cooldown=1200;
	public static final double WARP_EXIT_SPEED = .75; //px/ms
	public static final double CloseEnoughDistance = 100.0;
	
	//missile setup
	public final static int MISSILE_DAMAGE = 10;
	public final static double INITIAL_MISSILE_SPEED = .04;
	
	//Used by Shipyard
	public static final int queue_capa=5;//the capacity of a shipyard's queue
	
	//Defaults for Bases
	public final static int initial_soldier=100;
	public final static int default_max_soldier=500;
	public final static float soldier_production_rate = .005f; //solders per millisecond.  corresponds to 5 soldiers per second
	public final static int initial_base_endu=500;
	public final static int initial_shipyard_endu = 250;
	public final static int initial_mine_endu = 150;
	public final static int initial_taxoffice_endu = 100;
	public final static int initial_research_building_endu = 200;
	public final static int max_soldier_upgraderate=100;
	public final static int endu_upgraderate=100;
	public final static double additional_mine_penalty = .9;
	public final static double additional_taxoffice_penalty = .9;
	public final static int planet_building_limit = 7;
	//For MathFormula
	public final static double rand_mod = .05;
	//For Panel sizing
	public final static int mini_ship_w=50;
	public final static int mini_ship_h=75;
	public final static int mini_prog_w=75;
	
	//***************************************************************GENERAL DATA*******************************************//
	
	//used by GameControl for networking
	public static final int DEFAULT_PORT_NUMBER = 7007; //used for game server.
	public static final String DEFAULT_IP = "192.168.5.101";
}