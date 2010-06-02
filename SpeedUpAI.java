public class SpeedUpAI extends FlyerAI
{
	Flyer the_flyer;
	
	public SpeedUpAI(Flyer f)
	{
		the_flyer=f;
	}
	
	public double calcDesiredDirectionChng()
	{
		return 0.0;
	}
	
	public double calcDesiredSpeed(double dir_chng)
	{
		return GalacticStrategyConstants.WARP_EXIT_SPEED;
	}
}