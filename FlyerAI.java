public strictfp abstract class FlyerAI
{
	Flyer<?,?,?> the_flyer;
	
	static final int ABS_DIRECTION=0;
	static final int REL_DIRECTION=1;
	
	public abstract double calcDesiredDirection(long t);
	public abstract double calcDesiredSpeed(long t, double dir_chng);
	public abstract int directionType(); //specifies whether the calcDesiredDirection() returns the actual direction (ABS_DIRECTION) or the change in direction (REL_DIRECTION)
}