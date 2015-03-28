package galactic_strategy.game_objects;

import galactic_strategy.Constants;

public strictfp class SpeedUpAI extends FlyerAI
{
	public SpeedUpAI(){}
	
	public double calcDesiredDirection(long t)
	{
		return 0.0;
	}
	
	public int directionType()
	{
		return FlyerAI.REL_DIRECTION;
	}
	
	public double calcDesiredSpeed(long t, double dir_chng)
	{
		return Constants.WARP_EXIT_SPEED;
	}
}