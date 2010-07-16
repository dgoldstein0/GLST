import java.util.*;

public class Missile extends Flyer<Missile, Missile.MissileId>
{
	private final double Collide_Range=10.0;
	MissileId id;

	public Missile(Ship s, Targetable<?> t, long time)
	{
		super("", ShipType.MISSILE);
		
		//set up ship_data array, as per the subclass' responsibility
		data_control = new MissileDataSaverControl(this);
		
		location = s.location;
		id= new MissileId(s.next_missile_id++, s);
		location.next_missile_id++;
		
		//set up physics
		pos_x = s.getPos_x();
		pos_y = s.getPos_y();
		direction = s.getDirection();
		speed = s.getSpeed() + GalacticStrategyConstants.INITIAL_MISSILE_SPEED;
		location = s.location;
		
		target = t;
		destination=t;
		t.addAggressor(this);
		time = (long)(Math.ceil((double)(time)/(double)(GalacticStrategyConstants.TIME_GRANULARITY))*GalacticStrategyConstants.TIME_GRANULARITY);
		dest_x_coord = destination.getXCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
		dest_y_coord = destination.getYCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
		current_flying_AI = new TrackingAI(this, 0.0, TrackingAI.NO_SLOWDOWN);
		
		this.time=time;
		data_control.saveData();
	}
	
	public Describer<Missile> describer()
	{
		return new MissileDescriber(this);
	}
	
	@Override
	public void removeFromGame(long t)
	{
		location.missiles.remove(id, t);
	}
	
	//returns true when the missile detonates, false otherwise
	public boolean update(long t, Iterator<MissileId> missileIteration)
	{
		if (time <= t)
		{
			moveIncrement();
			time += GalacticStrategyConstants.TIME_GRANULARITY;
			
			if (collidedWithTarget())
			{
				detonate(missileIteration);
				data_control.saveData();
				return true;
			}
			else
			{
				data_control.saveData();
				return false;
			}
		}
		else
			return false;
	}
	
	//BOOKMARK: POTENTIAL COORDINATION HAZARD - if missile/ship update order is changed, this won't work
	public boolean collidedWithTarget()
	{
		//can use current x/y coords for ships because ship positions are updated first
		double x_dif=this.pos_x-target.getXCoord(time);
		double y_dif=this.pos_y-target.getYCoord(time);
		return (x_dif*x_dif+y_dif*y_dif<Collide_Range*Collide_Range);
	}
	
	public void detonate(Iterator<MissileId> missileIteration)
	{
		//this function could start an explosion animation instead
		missileIteration.remove();
		target.removeAggressor(this);
		
		//addDamage is called last to avoid a ConcurrentModificationException.  If the additional damage
		//destroys the target, then this kicks off an iteration through the remaining aggressors.  The missiles,
		//right now, destroyed() themselves when notified that their targetIsDestroyed(long).  because iteration.remove
		//is called, without removeAggressor(this), this addDamage call would cause the program to try to remove the
		//element from the missiles hashtable a second time - even though the iteration is using that element at the moment.
		target.addDamage(time, GalacticStrategyConstants.MISSILE_DAMAGE);
		
		is_alive=false;
	}
	
	public void destroyed()
	{
		synchronized(location.missiles)
		{
			location.missiles.remove(id, time); //must call remove with the Key and not the Value
		}
		is_alive=false;
		data_control.saveData(); //TODO: examine consequences of this
	}
	
	public void targetIsDestroyed(long t)
	{
		destroyed();
	}
	
	public void targetHasWarped(long t)
	{
		destroyed();
	}
	
	public static class MissileId extends FlyerId<MissileId>
	{
		private Ship shooter;
		private int m_id;
		
		public MissileId(int id, Ship s)
		{
			shooter = s;
			m_id = id;
		}
		
		public int hashCode()
		{
			return shooter.hashCode()*200 + m_id;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof MissileId)
			{
				return (((MissileId)o).shooter == shooter) && (((MissileId)o).m_id == m_id);
			}
			else
				return false;
		}

		public void setM_id(int m_id){this.m_id = m_id;}
		public int getM_id() {return m_id;}
		public void setShooter(Ship shooter) {this.shooter = shooter;}
		public Ship getShooter() {return shooter;}
	}
}