public class Missile extends Flyer
{
	private final double Collide_Range=5;
	public Missile(Targetable t)
	{
		target = t;
		type = GalacticStrategyConstants.missile_type;
		current_flying_AI = new TrackingAI(this);		
	}
	
	
	public void move(long t)
	{
		boolean c=false;
		while (time < t)
		{
			c=collidedWithTarget();
			if (!c)
			moveIncrement();
			else
				destroyed();
				
		}
		if (c)
			destroyed();
	}
	
	public boolean collidedWithTarget()
	{
		return (Math.hypot(this.pos_x-target.getXCoord(time),this.pos_x-target.getYCoord(time))<Collide_Range);
	}
	
	public void destroyed()
	{
		location.missiles.remove(this);
	    
	}
}