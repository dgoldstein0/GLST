public abstract class FlyerAI
{
	Flyer the_flyer;
	
	public abstract double calcDesiredDirectionChng();
	public abstract double calcDesiredSpeed(double dir);
}