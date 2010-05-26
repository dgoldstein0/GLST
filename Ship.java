import java.util.HashSet;

public class Ship extends Flyer implements Selectable
{
	final static double ESCAPE_DIST = 400.0;
	
	Player owner;
	
	int energy;
	int max_energy;
	long nextAttackingtime;
	
	float soldier;
	int mode;
		final static int MOVING =0;
		final static int ATTACKING =1;
		final static int TRAVEL_TO_WARP=2;
		final static int IN_WARP=3;
		final static int EXIT_WARP=4;
	
	//used for warping
	GSystem warp_destination;
	double exit_vec_x; //exit_vec ends up being a unit vector after orderToWarp, and the length is stored in exit_vec_len
	double exit_vec_y;
	double exit_vec_len;
	double exit_direction;
	long arrival_time;
	
	public Ship(String nm, ShipType t, int id)
	{
		super(nm,t);
		this.id=id;
		energy = t.max_energy;
		max_energy = energy;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		nextAttackingtime=0;
	}
	
	public DestDescriber describer(){return new ShipDescriber(owner, this);}
	
	public void assemble(Shipyard builder, long t)
	{
		//the time dependence of this function needs to be established
		owner=builder.location.owner;
		
		//set the position of the planet/moon correctly.  we do not need to restore the position, because in the updateGame function the orbit.move command is given after all facilities are updated
		builder.location.orbit.move(t);
		if(builder.location instanceof Moon)
			((Planet)builder.location.orbit.boss).orbit.move(t);
		pos_x = builder.default_x + builder.location.absoluteCurX();
		pos_y = builder.default_y + builder.location.absoluteCurY();
		double vel_x=builder.location.orbit.getAbsVelX();
		double vel_y=builder.location.orbit.getAbsVelY();
		direction = Math.atan2(vel_y, vel_x);
		speed = Math.hypot(vel_x, vel_y);
		
		if(builder.location instanceof Planet)
			location = (GSystem)builder.location.orbit.boss;
		else //builder.location is a Moon
			location = (GSystem) ((Planet)builder.location.orbit.boss).orbit.boss;
		location.fleets[owner.getId()].add(this);
		orderToMove(t, builder.location); //this call does not go via the game Order handling system.  all computers should issue these orders on their own.
	}
	
	//updates the ship an increment towards time t - moving and attacking.  return value is ignored
	public boolean update(long t)
	{
		/*this if statement is necessary incase game is updated too slow.  For instance, update last to 60, then
		an order is given which advances the ship to 80., but the next time all are updated to is 100, we want the
		ship ordered to move to only be updated once and not twice*/
		if(time <= t)
		{
			moveIncrement();
			switch(mode)
			{
				case ATTACKING:
					attack(time);
					break;
				case TRAVEL_TO_WARP:
					if(clearToWarp())
						engageWarpDrive();
					break;
				case EXIT_WARP:
					if(speed <= type.max_speed)
						mode=MOVING;
					break;
			}
			time += GalacticStrategyConstants.TIME_GRANULARITY;
			
			//save data
			saveData();
		}
		return false;
	}
	
	//this function is NOT incremental, i.e. it is only called once during updateGame() - before the updateGame function cycles through the systems
	public void moveDuringWarp(long t)
	{
		if(mode==IN_WARP)
		{
			if(t >= arrival_time)
			{
				disengageWarpDrive();
				advanceTime(arrival_time); //takes care of saving data and advancing time for us
			}
			else
			{
				double dist_moved = type.warp_speed*(t-time);
				pos_x += dist_moved*exit_vec_x;
				pos_y += dist_moved*exit_vec_y;
				time=t;
			}
		}
	}
	
	public void orderToMove(long t, Destination d)
	{
		if(mode != EXIT_WARP && mode != IN_WARP) //to ensure if the interface tries to issue an order, it can't
		{
			destination = d;
			advanceTime(t);
			//System.out.println(Integer.toString(id) + " orderToMove: t is " + Long.toString(t) + " and time is " + Long.toString(time));
			dest_x_coord = d.getXCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
			dest_y_coord = d.getYCoord(time-GalacticStrategyConstants.TIME_GRANULARITY);
			current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.LANDING_RANGE, TrackingAI.MATCH_SPEED);
			
			mode=MOVING;
			//current_flying_AI = new PatrolAI(this, 400.0, 300.0, 100.0, 1);
		}
	}
	
	public void orderToAttack(long t, Targetable tgt)
	{
		if(mode != EXIT_WARP && mode != IN_WARP)
		{
			//System.out.println(Integer.toString(id) + "orderToAttack: t is " + Long.toString(t));
			
			target= tgt;
			destination = tgt;
			mode=ATTACKING;
			target.addAggressor(this);
			advanceTime(t);
			nextAttackingtime = time;
			nextAttackingtime+=GalacticStrategyConstants.Attacking_cooldown;
			
			current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.Attacking_Range-5, TrackingAI.STOP);
		}
	}
	
	public void orderToWarp(long t, GSystem sys)
	{
		if(mode != EXIT_WARP && mode != IN_WARP)
		{
			//NEED TO IMPLEMENT
			advanceTime(t);
			warp_destination=sys;
			
			//calculate exit vector
			exit_vec_x = sys.x-location.x;
			exit_vec_y = sys.y-location.y;
			
			//make exit vector a unit vector - for later use
			exit_vec_len = Math.hypot(exit_vec_x, exit_vec_y);
			exit_vec_x /= exit_vec_len;
			exit_vec_y /= exit_vec_len;
			
			exit_direction = Math.atan2(exit_vec_x, exit_vec_y);
			
			current_flying_AI = new WarpAI(this);
		}
	}
	
	private void engageWarpDrive()
	{
		//This function works very much like the destroyed() function
		
		//remove from listing in system
		location.fleets[owner.getId()].remove(this); 
		
		//notify aggressors
		for(Targetter t : aggressors)
			t.targetHasWarped(time);
		
		//deselect the ship, if it was selected
		if(this == GameInterface.GC.GI.ShipPanel.the_ship)
		{
			GameInterface.GC.GI.displayNoPanel();
			GameInterface.GC.GI.selected_in_sys = null;
		}
		
		//compute details of flight plan
		pos_x=location.x;
		pos_y=location.y;
		arrival_time = time+(long)(exit_vec_len/type.warp_speed);
		
		owner.ships_in_transit.add(this);
	}
	
	private void disengageWarpDrive()
	{
		location=warp_destination;
		
		//rewrite physics values
		pos_x=-exit_vec_x*1.5*ESCAPE_DIST + location.absoluteCurX();
		pos_y=-exit_vec_y*1.5*ESCAPE_DIST + location.absoluteCurY();
		speed = GalacticStrategyConstants.WARP_EXIT_SPEED;
		direction = exit_direction; //should already be true, but just in case
		
		mode=EXIT_WARP;
		current_flying_AI = new StopAI();
		
		owner.ships_in_transit.remove(this);
		location.fleets[owner.getId()].add(this);
	}
	
	/*this function returns true IF:
		1)  exit_direction is the direction of the ship AND
			2) the component of the vector from the center of the system perpendicular
				to the exit vector is longer than the escape distance
		OR	3) the length of the vector from the center of the system is greater than the
				escape distance AND the angle between this and the exit vector is
				greater than 90 degrees.
	*/
	
	private boolean clearToWarp()
	{
		if(direction != exit_direction)
			return false;
		
		double radial_vec_x = pos_x-location.absoluteCurX();
		double radial_vec_y = pos_y-location.absoluteCurY();
		
		if(radial_vec_x*radial_vec_x + radial_vec_y*radial_vec_y > ESCAPE_DIST*ESCAPE_DIST)
		{
			double dot_product = radial_vec_x*exit_vec_x + radial_vec_y*exit_vec_y;
			if(dot_product < 0)
				//the dot product of the radial vector and the exit vector is negative
				//means the cosine of the angle between the vectors is negative and thus the
				//angle between them is greater than 90 degrees
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
			synchronized(location.missile_lock)
			{
				location.missiles.put(m.id,m);
			}
			nextAttackingtime= time+GalacticStrategyConstants.Attacking_cooldown;
		}
	}
	
	
	public void destroyed()
	{
		//System.out.println("destroyed-before");	
		if(location.fleets[owner.getId()].remove(this))//in case another attack has already destroyed the ship, but both call the destroyed method
		{
			//notify aggressors
			for(Targetter t : aggressors)
				t.targetIsDestroyed(time);
			
			//notify interface
			if(this == GameInterface.GC.GI.ShipPanel.the_ship)
			{
				GameInterface.GC.GI.displayNoPanel();
				GameInterface.GC.GI.selected_in_sys = null;
			}			
			//System.out.println("destroyed-after");
		}
	}
	
	public void targetIsDestroyed(long t)
	{
		mode=MOVING;
		if (destination==target)
		{
			destination=new DestinationPoint(target.getXCoord(t),target.getYCoord(t));
		}
	}
	
	public void targetHasWarped(long t)
	{
		mode=MOVING;
		if (destination==target)
		{
			destination=new DestinationPoint(target.getXCoord(t),target.getYCoord(t));
		}
	}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	//for interface Selectable
	public int getSelectType(){return Selectable.SHIP;}
	public String generateName(){return "[" + type.name +" #" + Integer.toString(id) + "] " + name;}
	
	//methods required for save/load
	public Ship(){}
	public int getEnergy(){return energy;}
	public void setEnergy(int f){energy=f;}
	public int getMax_energy(){return max_energy;}
	public void setMax_energy(int mf){max_energy=mf;}
	public Player getOwner() {return owner;}
	public float getSoldier() {return soldier;}
	public void setId(int i){id=i;}
	public int getId(){return id;}
	public int getMode(){return mode;}
	public void setMode(int m){mode=m;}
	public void setExit_vec_x(double x){exit_vec_x = x;}
	public double getExit_vec_x(){return exit_vec_x;}
	public void setExit_vec_y(double y){exit_vec_y = y;}
	public double getExit_vec_y(){return exit_vec_y;}
	public void setExit_direction(double d){exit_direction=d;}
	public double getExit_direction(){return exit_direction;}
}