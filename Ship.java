import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Random;

import javax.swing.SwingUtilities;

public class Ship extends Flyer<Ship, Ship.ShipId> implements Selectable
{
	final static double ESCAPE_DIST = 300.0;
	final static double EXIT_MULTIPLIER = 2.0; //the multiple of ESCAPE_DIST at which ships exit from warp
	final static double EXIT_PLACE_JITTER = 50.0;
	final static double EXIT_DIRECTION_JITTER = 0.2;
	
	Player owner;
	
	int energy;
	int max_energy;
	long nextAttackingtime;
	int next_missile_id;
	
	float soldier;
	public static enum MODES {MOVING, ATTACKING, TARGETTING_TARGET_LOST, TRAVEL_TO_WARP, ENTER_WARP, IN_WARP, EXIT_WARP, PICKUP_TROOPS;}
	MODES mode;
	
	/**this enum is for cases where targets warp/are destroyed/are lost for some reason
	 * before the order to attack them can be executed*/
	public static enum LOST_REASON {WARPED, DESTROYED;}
	Targetable<?> was_target; //used with modes TARGETTING_TARGET_DESTROYED and TARGETTING_TARGET_WARPED
	
	//used for warping
	GSystem warp_destination;
	double exit_vec_x; //exit_vec ends up being a unit vector after orderToWarp, and the length is stored in exit_vec_len
	double exit_vec_y;
	double exit_vec_len;
	double exit_direction;
	long arrival_time;
	
	public Ship(ShipType t)
	{
		super(t.name,t);
		
		data_control = new ShipDataSaverControl(this);
		
		energy = t.max_energy;
		max_energy = energy;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		nextAttackingtime=0;
		next_missile_id=0;
	}
	
	public Describer<Ship> describer(){return new ShipDescriber(owner, this);}
	
	//the time dependence of this function needs to be established
	public void assemble(Shipyard builder, long t)
	{
		owner=builder.location.owner;
			
		//set the position of the planet/moon correctly.  we do not need to restore the position, because in the updateGame function the orbit.move command is given after all facilities are updated
		//TODO: replace with recursive functions
		builder.location.orbit.move(t);
		if(builder.location instanceof Moon)
			((Planet)builder.location.orbit.boss).orbit.move(t);
		
		pos_x = builder.default_x + builder.location.absoluteCurX();
		pos_y = builder.default_y + builder.location.absoluteCurY();
		double vel_x=builder.location.orbit.getAbsVelX();
		double vel_y=builder.location.orbit.getAbsVelY();
		direction = Math.atan2(vel_y, vel_x);
		speed = Math.hypot(vel_x, vel_y);
		
		//TODO: replace with recursive functions
		if(builder.location instanceof Planet)
			location = (GSystem)builder.location.orbit.boss;
		else //builder.location is a Moon
			location = (GSystem) ((Planet)builder.location.orbit.boss).orbit.boss;
		
		location.fleets[owner.getId()].add(this, t);
		time = GameInterface.GC.TC.getTimeGrainAfter(t);
		orderToMove(time, builder.location); //this call does not go via the game Order handling system.  all computers should issue these orders on their own.
	}
	
	//updates the ship an increment towards time t - moving and attacking.  return value is meaningless/ignored
	//DOES NOT SAVE DATA
	public boolean update(long t, Fleet.ShipIterator shipIteration)
	{
		//TODO: update this comment
		/*this if statement is necessary in case game is updated too slow.  For instance, update last to 60, then
		an order is given which advances the ship to 80., but the next time all are updated to is 100, we want the
		ship ordered to move to only be updated once and not twice*/
		if(time < t)
		{
			moveIncrement();
			switch(mode)
			{
				case TARGETTING_TARGET_LOST:
					mode = MODES.MOVING;
					was_target=null;
					break;
				case ATTACKING:
					attack(time);
					break;
				case TRAVEL_TO_WARP:
					if(isClearToWarp())
					{
						System.out.println("clear to warp!");
						mode=MODES.ENTER_WARP;
						current_flying_AI = new SpeedUpAI();
					}
					break;
				case ENTER_WARP:
					if(fastEnoughToWarp())
						engageWarpDrive(shipIteration);
					break;
				case EXIT_WARP:
					if(speed <= type.max_speed)
						mode=MODES.MOVING;
					break;
				case PICKUP_TROOPS:
					if(!doTransferTroops() || soldier == type.soldier_capacity)
						mode=MODES.MOVING;
					break;
			}
			time += GalacticStrategyConstants.TIME_GRANULARITY;
			data_control.saveData();
		}
		return false;
	}
	
	//this function is called when time is rolled back to before the ship existed
	@Override
	public void removeFromGame(long t)
	{
		location.fleets[owner.getId()].remove(this, t);
	}
	
	//this function is NOT incremental, i.e. it is only called once during updateGame() - before the updateGame function cycles through the systems
	//this function returns true if the ship exits warp, false otherwise.
	public boolean moveDuringWarp(long t, Iterator<Ship> ship_it)
	{
		//System.out.println("move during warp");
		if(t > time && mode==MODES.IN_WARP)
		{
			//System.out.println("...warping...");
			if(t >= arrival_time)
			{
				//System.out.println("arriving...")
				disengageWarpDrive(ship_it);
				time = GameInterface.GC.TC.getTimeGrainAfter(arrival_time);
				return true;
			}
			else
			{
				double dist_moved = type.warp_speed*(t-time);
				pos_x += dist_moved*exit_vec_x;
				pos_y += dist_moved*exit_vec_y;
				time=t;
			}
			data_control.saveData();
		}
		return false;
	}
	
	private boolean fastEnoughToWarp()
	{
		return (speed >= GalacticStrategyConstants.WARP_EXIT_SPEED);
	}
	
	protected double getAccel()
	{
		if(mode==MODES.ENTER_WARP || mode== MODES.EXIT_WARP)
			return type.warp_accel;
		else
			return type.accel_rate;
	}
	
	protected boolean enforceSpeedCap()
	{
		return (mode != MODES.ENTER_WARP); //only enforce if mode is NOT enter warp, i.e. ships can go superspeed when warping
	}
	
	public void orderToMove(long t, Destination<?> d)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP) //to ensure if the interface tries to issue an order, it can't
		{
			destination = d;
			
			if(target != null)
			{
				target.removeAggressor(this);
				target=null;
			}
			
			//System.out.println(Integer.toString(id) + " orderToMove: t is " + Long.toString(t) + " and time is " + Long.toString(time));
			dest_x_coord = d.getXCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
			dest_y_coord = d.getYCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
			current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.LANDING_RANGE, TrackingAI.MATCH_SPEED);
			
			mode=MODES.MOVING;
			data_control.saveData();
			//current_flying_AI = new PatrolAI(this, 400.0, 300.0, 100.0, 1);
		}
	}
	
	public void orderToAttack(long t, Targetable<?> tgt)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP)
		{
			//System.out.println(Integer.toString(id) + "orderToAttack: t is " + Long.toString(t));
			
			target= tgt;
			destination = tgt;
			mode=MODES.ATTACKING;
			target.addAggressor(this);
			nextAttackingtime = time;
			nextAttackingtime+=GalacticStrategyConstants.Attacking_cooldown;
			
			//TODO: get constant 5 out of here
			current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.Attacking_Range-5, TrackingAI.STOP);
			data_control.saveData();
		}
	}
	
	public void orderToWarp(long t, GSystem sys)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP && sys != location) //if sys == location, user means to cancel warp command.
		{
			if(target != null)
			{
				target.removeAggressor(this);
				target=null;
			}
			
			mode=MODES.TRAVEL_TO_WARP;
			warp_destination=sys;
			
			//calculate exit vector
			exit_vec_x = sys.x-location.x;
			exit_vec_y = sys.y-location.y;
			
			//make exit vector a unit vector - for later use
			exit_vec_len = Math.hypot(exit_vec_x, exit_vec_y);
			exit_vec_x /= exit_vec_len;
			exit_vec_y /= exit_vec_len;
			
			exit_direction = Math.atan2(exit_vec_y, exit_vec_x);
			
			current_flying_AI = new WarpAI(this);
			data_control.saveData();
		}
	}
	
	public void orderToInvade(OwnableSatellite<?> sat, long t)
	{
		if(mode == MODES.MOVING || mode == MODES.ATTACKING)
		{
			double x_dif = pos_x-sat.getXCoord(t);
			double y_dif = pos_y-sat.getYCoord(t);
			if(x_dif*x_dif + y_dif*y_dif < GalacticStrategyConstants.LANDING_RANGE*GalacticStrategyConstants.LANDING_RANGE)
			{
				if(sat.getOwner() != null)
				{
					synchronized(sat.facilities_lock)
					{
						if(sat.the_base == null) //if base isn't finished being built, player can take over without a fight
							sat.setOwner(getOwner(), t);
						else
							sat.the_base.attackedByTroops(GameInterface.GC.TC.getNextTimeGrain(), this);
					}
				}
				else
				{
					((OwnableSatellite<?>)destination).setOwner(getOwner(), t);
				}
			}
			data_control.saveData();
		}
	}
	
	public void orderToPickupTroops(long t) {
		
		if(mode == MODES.MOVING &&
				destination instanceof OwnableSatellite<?> &&
				((OwnableSatellite<?>)destination).getOwner() == owner &&
				((OwnableSatellite<?>)destination).the_base != null &&
				soldier < type.soldier_capacity)
		{
			mode = MODES.PICKUP_TROOPS;
			data_control.saveData();
		}
	}
	
	//returns true for success, false if the destination is no longer owned by the player or the base has been destroyed
	private boolean doTransferTroops()
	{
		if(	((OwnableSatellite<?>)destination).getOwner() == owner &&
				((OwnableSatellite<?>)destination).the_base != null)
		{
			double dif_x = dest_x_coord-pos_x;
			double dif_y = dest_y_coord-pos_y;
			if(dif_x*dif_x + dif_y*dif_y <= GalacticStrategyConstants.LANDING_RANGE*GalacticStrategyConstants.LANDING_RANGE)
			{
				float get_soldiers = Math.min(type.soldier_capacity - soldier, GalacticStrategyConstants.troop_transfer_rate*GalacticStrategyConstants.TIME_GRANULARITY);
				synchronized(((OwnableSatellite<?>)destination).facilities_lock)
				{
					soldier += ((OwnableSatellite<?>)destination).the_base.retrieveSoldiers(time+GalacticStrategyConstants.TIME_GRANULARITY, get_soldiers, this);
				}
				data_control.saveData();
			}
			return true;
		}
		else
			return false;
	}
	
	public int warpRange(){return type.warp_range;}
	
	private void engageWarpDrive(Fleet.ShipIterator shipIteration)
	{
		//This function works very much like the destroyed() function
		System.out.println("Engaging warp drive....");
		
		//remove from listing in system
		if(shipIteration != null)
			shipIteration.remove(time); //remove via the iterator to avoid ConcurrentModificationException
		else
			location.fleets[owner.getId()].remove(this, time);
		
		//notify aggressors
		for(Targetter<?> t : aggressors)
			t.targetHasWarped(time);
		
		aggressors.clear();
		
		//deselect the ship, if it was selected
		try {
			SwingUtilities.invokeAndWait(new ShipDeselector(this));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//compute details of flight plan
		pos_x=location.x;
		pos_y=location.y;
		arrival_time = time+(long)(exit_vec_len/type.warp_speed);
		System.out.println("arrival time is " + Long.toString(arrival_time));
		
		mode=MODES.IN_WARP;
		owner.ships_in_transit.add(this);
		data_control.saveData();
	}
	
	private void disengageWarpDrive(Iterator<Ship> ship_it)
	{
		System.out.println("Disengaging warp drive");
		location=warp_destination;
		
		//set up random jitter with arrival_time as seed.  necessary to seed it to keep everything coordinated.
		Random generator = new Random(arrival_time);
		
		//rewrite physics values
		pos_x=-exit_vec_x*ESCAPE_DIST*EXIT_MULTIPLIER + location.absoluteCurX() + EXIT_PLACE_JITTER*generator.nextGaussian();
		pos_y=-exit_vec_y*ESCAPE_DIST*EXIT_MULTIPLIER + location.absoluteCurY() + EXIT_PLACE_JITTER*generator.nextGaussian();
		speed = GalacticStrategyConstants.WARP_EXIT_SPEED;
		direction = exit_direction + EXIT_DIRECTION_JITTER*generator.nextGaussian(); //should already be true, but just in case
		
		mode=MODES.EXIT_WARP;
		current_flying_AI = new StopAI();
		
		ship_it.remove();//owner.ships_in_transit.remove(this);
		location.fleets[owner.getId()].add(this, arrival_time);
		
		//no data_control.saveData() here since this method is only called from moveDuringWarp and that does the saveData
	}
	
	/*this function returns true IF:
		1)  exit_direction is the direction of the ship AND
			2) the component of the vector from the center of the system perpendicular
				to the exit vector is longer than the escape distance
		OR	3) the length of the vector from the center of the system is greater than the
				escape distance AND the angle between this and the exit vector is
				less than 90 degrees. (remember - radial vec points from center to ship,
				not other way around)
	*/
	
	private boolean isClearToWarp()
	{
		if(direction != exit_direction)
			return false;
		
		double radial_vec_x = pos_x-location.absoluteCurX();
		double radial_vec_y = pos_y-location.absoluteCurY();
		
		if(radial_vec_x*radial_vec_x + radial_vec_y*radial_vec_y > ESCAPE_DIST*ESCAPE_DIST)
		{
			double dot_product = radial_vec_x*exit_vec_x + radial_vec_y*exit_vec_y;
			if(dot_product > 0)
				//the dot product of the radial vector and the exit vector is positive
				//means the cosine of the angle between the vectors is positive and thus the
				//angle between them is less than 90 degrees
				return true;
			else //check the component of the radial vector perpendicular to the exit vector.  It is
				//a precondition that the radial vector is longer than the ESCAPE_DIST for one of its
				//components to be larger too
			{
				//the projection of radial_vec onto exit_vec:
				double scale = dot_product/(exit_vec_x*exit_vec_x + exit_vec_y*exit_vec_y); //the ratio of the projection onto the exit_vec to the exit_vec
				double proj_x = scale*exit_vec_x;
				double proj_y = scale*exit_vec_y;
				
				//the components of the component of the radial vector perpendicular to the exit_vec
				double perp_x = radial_vec_x - proj_x;
				double perp_y = radial_vec_y - proj_y;
				
				if(perp_x*perp_x+perp_y*perp_y > ESCAPE_DIST*ESCAPE_DIST)
					return true;
			}
		}
		
		return false;
	}
	
	public void attack(long t)
	{
		double dx = destinationX() - pos_x;
		double dy = destinationY() - pos_y;
		if ((dx*dx+dy*dy<GalacticStrategyConstants.Attacking_Range*GalacticStrategyConstants.Attacking_Range)&&(nextAttackingtime<=t))
		{
			Missile m=new Missile(this, target, time+GalacticStrategyConstants.TIME_GRANULARITY); 
			location.missiles.put(m.id, m, t);
			nextAttackingtime= time+GalacticStrategyConstants.Attacking_cooldown;
		}
	}
	
	
	public void destroyed()
	{
		System.out.println("destroyed-before");	
		if(location.fleets[owner.getId()].remove(this, time))//if is so in case another attack has already destroyed the ship, but both call the destroyed method
		{
			is_alive=false;
			
			//notify aggressors
			for(Targetter<?> t : aggressors)
				t.targetIsDestroyed(time);
			
			//notify interface
			try {
				SwingUtilities.invokeAndWait(new ShipDeselector(this));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			System.out.println("destroyed-after");
		}
	}
	
	public class ShipDeselector implements Runnable
	{
		final Ship the_ship;
		
		public ShipDeselector(Ship s)
		{
			the_ship = s;
		}
		
		public void run()
		{
			if(the_ship == GameInterface.GC.GI.ShipPanel.the_ship)
			{
				GameInterface.GC.GI.displayNoPanel();
			}
			GameInterface.GC.GI.selected_in_sys.remove(this);	
		}
	}
	
	@Override
	public void targetIsDestroyed(long t){targetIsDestroyed(t, false, null);}
	
	public void targetIsDestroyed(long t, boolean late_order, Targetable<?> tgt)
	{
		targetLost(LOST_REASON.DESTROYED, t, late_order, tgt);
	}
	
	@Override
	public void targetHasWarped(long t){targetHasWarped(t, false, null);}
	
	public void targetHasWarped(long t, boolean late_order, Targetable<?> tgt)
	{
		targetLost(LOST_REASON.WARPED, t, late_order, tgt);
	}
	
	public void targetLost(LOST_REASON reason, long t, boolean late_order, Targetable<?> tgt /*ignored if late_order is false*/)
	{
		//System.out.println("target lost");
		if (destination==(Destination<?>)target)
		{
			//System.out.println("\tchanging destination...");
			destination=new DestinationPoint(target.getXCoord(t),target.getYCoord(t));
			SwingUtilities.invokeLater(new DestUpdater(this));
		}
		
		if(!late_order)
			mode=MODES.MOVING;
		else
		{
			mode = MODES.TARGETTING_TARGET_LOST;
			was_target = tgt;
		}
		
		target=null;
		data_control.saveData();
		//TODO: player notification - THIS SHOULD USE LOST_REASON
	}
	
	private class DestUpdater implements Runnable
	{
		final Ship the_ship;
		
		private DestUpdater(Ship s)
		{
			the_ship=s;
		}
		
		public void run()
		{
			if(GameInterface.GC.GI.ShipPanel.the_ship == the_ship)
			{
				GameInterface.GC.GI.ShipPanel.updateDestDisplay(the_ship.destination);
			}
		}
	}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	//for interface Selectable
	public int getSelectType(){return Selectable.SHIP;}
	public String generateName(){return "[" + type.name + "] " + name;}
	
	//methods required for save/load
	public Ship(){}
	public int getEnergy(){return energy;}
	public void setEnergy(int f){energy=f;}
	public int getMax_energy(){return max_energy;}
	public void setMax_energy(int mf){max_energy=mf;}
	public Player getOwner() {return owner;}
	public float getSoldier() {return soldier;}
	public MODES getMode(){return mode;}
	public void setMode(MODES m){mode=m;}
	public void setExit_vec_x(double x){exit_vec_x = x;}
	public double getExit_vec_x(){return exit_vec_x;}
	public void setExit_vec_y(double y){exit_vec_y = y;}
	public double getExit_vec_y(){return exit_vec_y;}
	public void setExit_direction(double d){exit_direction=d;}
	public double getExit_direction(){return exit_direction;}
	
	public static class ShipId extends FlyerId<ShipId>
	{
		Shipyard manufacturer;
		int queue_id;
		
		public ShipId(int q_id, Shipyard manu)
		{
			manufacturer = manu;
			queue_id = q_id;
		}
		
		@Override
		public int hashCode()
		{
			if(manufacturer != null)
				return manufacturer.hashCode()*211 + queue_id;
			else
				return 0;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof ShipId)
			{
				ShipId s = (ShipId)o;
				return (s.manufacturer == manufacturer) && (s.queue_id == queue_id);
			}
			else //this will catch the case where o is null
				return false;
		}

		public void setManufacturer(Shipyard manufacturer) {this.manufacturer = manufacturer;}
		public Shipyard getManufacturer() {return manufacturer;}
		public void setQueue_id(int queue_id) {this.queue_id = queue_id;}
		public int getQueue_id() {return queue_id;}
	}
}