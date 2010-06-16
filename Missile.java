import java.util.*;

public class Missile extends Flyer
{
	private final double Collide_Range=10.0;

	public Missile(Ship s, Targetable t, long time)
	{
		super("", GalacticStrategyConstants.sTypes[GalacticStrategyConstants.MISSILE]);
		
		//set up ship_data array, as per the subclass' responsibility
		ship_data=new FlyerDataSaver[data_capacity];
		for(int i=0; i<ship_data.length; i++)
			ship_data[i] = new FlyerDataSaver();
		
		target = t;
		location = s.location;
		id=location.next_missile_id;
		location.next_missile_id++;
		
		//set up physics
		pos_x = s.getPos_x();
		pos_y = s.getPos_y();
		direction = s.getDirection();
		speed = s.getSpeed() + GalacticStrategyConstants.INITIAL_MISSILE_SPEED;
		location = s.location;
		
		destination=t;
		t.addAggressor(this);
		time = (long)(Math.ceil((double)(time)/(double)(GalacticStrategyConstants.TIME_GRANULARITY))*GalacticStrategyConstants.TIME_GRANULARITY);
		dest_x_coord = destination.getXCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
		dest_y_coord = destination.getYCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
		current_flying_AI = new TrackingAI(this, 0.0, TrackingAI.NO_SLOWDOWN);
		
		this.time=time;
		saveData(ALL_DATA);
	}
	
	public DestDescriber describer()
	{
		return new MissileDescriber(this);
	}
	
	//returns true when the missile detonates, false otherwise
	public boolean update(long t, Iterator<Integer> iteration)
	{
		if (time <= t)
		{
			moveIncrement();
			time += GalacticStrategyConstants.TIME_GRANULARITY;
			saveData(ALL_DATA);
			
			if (collidedWithTarget())
			{
				detonate(iteration);
				return true;
			}
			else
				return false;
		}
		return false;
	}
	
	public boolean collidedWithTarget()
	{
		//can use current x/y coords for ships because ship positions are updated first
		double x_dif=this.pos_x-target.getXCoord(time);
		double y_dif=this.pos_y-target.getYCoord(time);
		return (x_dif*x_dif+y_dif*y_dif<Collide_Range*Collide_Range);
	}
	
	public void detonate(Iterator<Integer> iteration)
	{
		//this function could start an explosion animation instead
		iteration.remove();
		target.removeAggressor(this);
		
		//addDamage is called last to avoid a ConcurrentModificationException.  If the additional damage
		//destroys the target, then this kicks off an iteration through the remaining aggressors.  The missiles,
		//right now, destroyed() themselved when notified that their targetIsDestroyed(long).  because iteration.remove
		//is called, without removeAggressor(this), this addDamage call would cause the program to try to remove the
		//element from the missiles hashtable a second time - even though the iteration is using that element at the moment.
		target.addDamage(GalacticStrategyConstants.MISSILE_DAMAGE);
	}
	
	public void destroyed()
	{
		synchronized(location.missile_lock)
		{
			location.missiles.remove(id); //must call remove with the Key and not the Value
		}
	}
	
	public void targetIsDestroyed(long t)
	{
		destroyed();
	}
	
	public void targetHasWarped(long t)
	{
		destroyed();
	}
}