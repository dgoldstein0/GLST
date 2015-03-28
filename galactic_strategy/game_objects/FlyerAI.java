package galactic_strategy.game_objects;
public strictfp abstract class FlyerAI
{
	FlyingThing flying_thing;
	
	static final int ABS_DIRECTION=0;
	static final int REL_DIRECTION=1;
	
	public FlyerAI(FlyingThing f)
	{
		flying_thing = f;
	}
	
	public abstract double calcDesiredDirection(long t);
	public abstract double calcDesiredSpeed(long t, double dir_chng);
	public abstract int directionType(); //specifies whether the calcDesiredDirection() returns the actual direction (ABS_DIRECTION) or the change in direction (REL_DIRECTION)

	public FlyerAI() {}
	public FlyingThing getFlyingThing(){return flying_thing;}
	public void setFlyingThing(FlyingThing f){flying_thing = f;}
}