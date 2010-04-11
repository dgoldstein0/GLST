public class Missile extends Flyer
{
	private final double Collide_Range=5.0;
	int id;
	
	public Missile(Ship s, Targetable t, long time, int i)
	{
		target = t;
		current_flying_AI = new TrackingAI(this);
		id = i;

		SetUpFlyer("", GalacticStrategyConstants.sTypes[GalacticStrategyConstants.MISSILE]);
		
		//set up physics
		pos_x = s.getPos_x();
		pos_y = s.getPos_y();
		direction = s.getDirection();
		speed = s.getSpeed();
		location = s.location;
		
		destination=t;
		time = (long)(Math.ceil((double)(time)/(double)(time_granularity))*time_granularity);
		dest_x_coord = destination.getXCoord(time-time_granularity);
		dest_y_coord = destination.getYCoord(time-time_granularity);
		current_flying_AI = new TrackingAI(this);
		
		this.time=time;
	}
	
	
	public boolean move(long t)
	{
		boolean c=false;
		while (time < t)
		{
			c=collidedWithTarget();
			if (!c)
				moveIncrement();
			else
				return true;
				
		}
		c=collidedWithTarget();
		if (c)
			return true;
		else
			return false;
	}
	
	public boolean collidedWithTarget()
	{
		//can use current x/y coords for ships because ship positions are updated first
		return (Math.hypot(this.pos_x-target.getXCoord(time),this.pos_y-target.getYCoord(time))<Collide_Range);
	}
	
	public void destroyed()
	{
		//this function could start an explosion animation
		target.addDamage(GalacticStrategyConstants.MISSILE_DAMAGE);
		synchronized(location.missile_lock)
		{
			location.missiles.remove(id);
		}
	}
	
	public void targetIsDestroyed()
	{
		synchronized(location.missile_lock)
		{
			location.missiles.remove(id);
		}
	}
}