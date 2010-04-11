public abstract class FlyerAI
{
	Flyer the_flyer;
	
	public abstract double calcDesiredDirection();
	public abstract double calcDesiredSpeed(double dir);
}