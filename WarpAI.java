public strictfp class WarpAI extends FlyerAI
{
	public WarpAI(Ship f)
	{
		super(f);
	}
	
	public double calcDesiredDirection(long t)
	{
		return ((Ship)the_flyer).exit_direction;
	}
	
	public int directionType()
	{
		return FlyerAI.ABS_DIRECTION;
	}
	
	public double calcDesiredSpeed(long t, double angle_chng)
	{
		double cosine = Math.cos(angle_chng);
		return the_flyer.type.max_speed*cosine*Math.abs(cosine);
	}
	
	public WarpAI(){}
}