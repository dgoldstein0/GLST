import java.util.*;

public strictfp class Missile extends Flyer<Missile, Missile.MissileId, Iterator<Missile.MissileId>>
{
	private static final double Collide_Range=10.0; //TODO: move this to GalacticStrategyConstants
	boolean target_alive;

	public Missile(Ship s, long time, Targetable<?> t)
	{
		super("", ShipType.MISSILE);
		
		location = s.location;
		id= new MissileId(s.next_missile_id++, s);
		owner = s.owner;
		
		//set up physics
		pos_x = s.getPos_x();
		pos_y = s.getPos_y();
		direction = s.getDirection();
		speed = s.getSpeed() + GalacticStrategyConstants.INITIAL_MISSILE_SPEED;
		location = s.location;
		
		target = t;
		destination=t;
		t.addAggressor(this);
		target_alive=true;
		
		//TODO: why do getXCoord and getYCoord take time values?
		dest_x_coord = destination.getXCoord();
		dest_y_coord = destination.getYCoord();
		current_flying_AI = new TrackingAI(this, 0.0, TrackingAI.IN_RANGE_BEHAVIOR.NO_SLOWDOWN);
	}
	
	public Missile(){}
	
	public Describer<Missile> describer()
	{
		return new MissileDescriber(this);
	}
	
	@Override
	public void removeFromGame()
	{
		location.missiles.remove(id);
	}
	
	//returns true when the missile detonates, false otherwise
	@Override
	public boolean update(long t, Iterator<MissileId> missileIteration)
	{
		boolean retval = false;
		
		moveIncrement(t);
		
		if (collidedWithTarget(t))
		{
			detonate(t, missileIteration);
			retval = true;
		}

		return retval;
	}
	
	//TODO: POTENTIAL COORDINATION HAZARD - if missile/ship update order is changed, this won't work
	public boolean collidedWithTarget(long t)
	{
		//can use current x/y coords for ships because ship positions are updated first
		//TODO: when is target not alive, and destination not a DestinationPoint?
		if(target_alive)
		{
			double x_dif=this.pos_x-target.getXCoord();
			double y_dif=this.pos_y-target.getYCoord();
			return (x_dif*x_dif+y_dif*y_dif<Collide_Range*Collide_Range);
		}
		else
		{
			if(destination instanceof DestinationPoint)
			{
				double x_dif=this.pos_x-((DestinationPoint)destination).getX();
				double y_dif=this.pos_y-((DestinationPoint)destination).getY();
				return (x_dif*x_dif+y_dif*y_dif<Collide_Range*Collide_Range);
			}
			else
			{
				return true;
			}
		}
	}
	
	public void detonate(long t, Iterator<MissileId> missileIteration)
	{
		//this function could start an explosion animation instead
		missileIteration.remove();
		if(target_alive)
		{
			target.removeAggressor(this);
			
			//addDamage is called last to avoid a ConcurrentModificationException.  If the additional damage
			//destroys the target, then this kicks off an iteration through the remaining aggressors.  The missiles,
			//right now, destroyed() themselves when notified that their targetIsDestroyed(long).  because iteration.remove
			//is called, without removeAggressor(this), this addDamage call would cause the program to try to remove the
			//element from the missiles hashtable a second time - even though the iteration is using that element at the moment.
			target.addDamage(t, GalacticStrategyConstants.MISSILE_DAMAGE);
		}
		
		is_alive=false;
	}
	
	/**not safe to call while iterating through location.missiles*/
	public void destroyed(long t)
	{
		synchronized(location.missiles)
		{
			location.missiles.remove(id); //must call remove with the Key and not the Value
		}
		is_alive=false;
	}
	
	@Override
	public void targetIsDestroyed(long t)
	{
		//also see Ship's targetIsDestroyed function.
		destination = new DestinationPoint(
						target.getXCoord(),
						target.getYCoord()
					);
		target_alive = false;
		target = null;
	}
	
	@Override
	public void targetHasWarped(long t)
	{
		destroyed(t);
	}
	
	public boolean getTarget_alive(){return target_alive;}
	public void setTarget_alive(boolean b){target_alive=b;}
	
	public static class MissileId extends Flyer.FlyerId<MissileId> implements Comparable<MissileId>
	{
		private Ship shooter;
		private int m_id;
		
		public MissileId(int id, Ship s)
		{
			shooter = s;
			m_id = id;
		}
		
		public MissileId(){}
		
		public int hashCode()
		{
			if(shooter != null)
				return shooter.hashCode()*211 + m_id;
			else
				return 0;
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

		@Override
		public int compareTo(MissileId o) {
			
			int shooter_comp = shooter.id.compareTo(o.shooter.id);
			if (shooter_comp != 0)
				return shooter_comp;
			
			if (m_id < o.m_id)
				return -1;
			else if (m_id > o.m_id)
				return 1;
			else
				return 0;
		}
	}
}