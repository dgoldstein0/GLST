package galactic_strategy.game_objects;

import galactic_strategy.Constants;
import galactic_strategy.sync_engine.Describer;
import galactic_strategy.sync_engine.MissileDescriber;

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
		flying_part.setInitialPositionAndVelocity(
				s.flying_part.getPos_x(),
				s.flying_part.getPos_y(),
				s.flying_part.getSpeed() + Constants.INITIAL_MISSILE_SPEED,
				s.flying_part.getDirection()
			);
		location = s.location;
		
		target = t;
		flying_part.setDestination(t);
		t.addAggressor(this);
		target_alive=true;
		
		setDest_x_coord(t.getXCoord());
		setDest_y_coord(t.getYCoord());
		flying_part.setCurrent_flying_AI(
			new TrackingAI(this.flying_part, 0.0, TrackingAI.IN_RANGE_BEHAVIOR.NO_SLOWDOWN)
		);
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
		
		flying_part.moveIncrement(t);
		
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
		//TODO: when is target not alive, and destination not a DestinationPoint?
		if(target_alive)
		{
			return (flying_part.findSqDistance(target)<Collide_Range*Collide_Range);
		}
		else
		{
			if(flying_part.getDestination() instanceof DestinationPoint)
			{
				return flying_part.findSqDistance(flying_part.getDestination()) < Collide_Range*Collide_Range;
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
			target.addDamage(t, Constants.MISSILE_DAMAGE);
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
		flying_part.setDestination(new DestinationPoint(
						target.getXCoord(),
						target.getYCoord()
					));
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

	@Override
	public boolean isInWarpTransition() {
		return false;
	}
}