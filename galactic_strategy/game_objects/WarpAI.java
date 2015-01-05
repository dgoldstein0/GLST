package galactic_strategy.game_objects;

public strictfp class WarpAI extends FlyerAI
{
	Ship the_ship;
	
	public WarpAI(Ship s)
	{
		super(s.flying_part);
		the_ship = s;
	}
	
	public double calcDesiredDirection(long t)
	{
		return the_ship.exit_direction;
	}
	
	public int directionType()
	{
		return FlyerAI.ABS_DIRECTION;
	}
	
	public double calcDesiredSpeed(long t, double angle_chng)
	{
		double cosine = Math.cos(angle_chng);
		return flying_thing.getCapabilities().getMaxSpeed()*cosine*Math.abs(cosine);
	}
	
	public WarpAI(){}
}