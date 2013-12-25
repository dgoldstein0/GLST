package galactic_strategy.game_objects;

public strictfp class StopAI extends FlyerAI
{
	public StopAI(){}
	
	public double calcDesiredDirection(long t){return 0.0;}
	public double calcDesiredSpeed(long t, double dir_chng){return 0.0;}
	public int directionType(){return FlyerAI.REL_DIRECTION;}
}