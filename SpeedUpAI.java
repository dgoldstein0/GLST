public class SpeedUpAI extends FlyerAI
{
	public SpeedUpAI(){}
	
	public double calcDesiredDirection()
	{
		return 0.0;
	}
	
	public int directionType()
	{
		return FlyerAI.REL_DIRECTION;
	}
	
	public double calcDesiredSpeed(double dir_chng)
	{
		return GalacticStrategyConstants.WARP_EXIT_SPEED;
	}
}