public class Missile extends Flyer
{
	public Missile(Targetable t)
	{
		target = t;
		type = GalacticStrategyConstants.missile_type;
		current_flying_AI = new TrackingAI(this);
	}
	
	public void destroyed()
	{
		location.missiles.remove(this);
	}
}