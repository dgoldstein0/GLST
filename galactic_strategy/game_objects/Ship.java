package galactic_strategy.game_objects;
import galactic_strategy.Constants;
import galactic_strategy.sync_engine.Describer;
import galactic_strategy.sync_engine.ShipDescriber;
import galactic_strategy.ui.GameInterface;
import galactic_strategy.ui.ImageResource;
import galactic_strategy.ui.Selectable;
import galactic_strategy.ui.ShipDeselector;

import java.util.Iterator;
import java.util.Random;

import javax.swing.SwingUtilities;

public strictfp class Ship extends Flyer<Ship, Ship.ShipId, Fleet.ShipIterator> implements Selectable
{
	final static double ESCAPE_DIST = 300.0;
	final static double EXIT_MULTIPLIER = 2.0; //the multiple of ESCAPE_DIST at which ships exit from warp
	final static double EXIT_PLACE_JITTER = 50.0;
	final static double EXIT_DIRECTION_JITTER = 0.2;
	
	int energy;
	int max_energy;
	long nextAttackingtime;
	int next_missile_id;
	
	float soldier;
	public static enum MODES {IDLE, MOVING, ORBITING, PROTECTORBIT, ATTACKING, USERATTACKING, ATTACKMOVE, USERATTACKMOVE, TARGET_LOST, TRAVEL_TO_WARP, 
		ENTER_WARP, IN_WARP, EXIT_WARP, PICKUP_TROOPS;}
	MODES mode;
	
	/**this enum is for cases where targets warp/are destroyed/are lost for some reason
	 * before the order to attack them can be executed*/
	public static enum LOST_REASON {WARPED, DESTROYED;}
	Targetable<?> was_target; //used with modes TARGETTING_TARGET_LOST
	
	//used for warping
	GSystem warp_destination;
	// x,y for warping
	private double warp_x;
	private double warp_y;
	double exit_vec_x; //exit_vec ends up being a unit vector after orderToWarp, and the length is stored in exit_vec_len
	double exit_vec_y;
	double exit_vec_len;
	double exit_direction;
	long arrival_time;
	AbstractDestination<?> SecondDest;
	
	public Ship(ShipType t, Ship.ShipId id)
	{
		super(t.name,t);
		this.id = id;
		
		energy = t.max_energy;
		max_energy = energy;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		nextAttackingtime=0;
		next_missile_id=0;
		SecondDest = null;
	}
	
	public Describer<Ship> describer(){return new ShipDescriber(owner, this);}
	
	public void assemble(Shipyard builder)
	{
		owner=builder.location.owner;
		
		double pos_x = builder.default_x + builder.location.absoluteCurX();
		double pos_y = builder.default_y + builder.location.absoluteCurY();
		double vel_x=builder.location.orbit.getAbsVelX();
		double vel_y=builder.location.orbit.getAbsVelY();
		double direction = Math.atan2(vel_y, vel_x);
		double speed = Math.hypot(vel_x, vel_y);
		flying_part.setInitialPositionAndVelocity(pos_x, pos_y, speed, direction);
		
		//TODO: replace with recursive functions
		if(builder.location instanceof Planet)
			location = (GSystem)builder.location.orbit.boss;
		else //builder.location is a Moon
			location = (GSystem) ((Planet)builder.location.orbit.boss).orbit.boss;
		
		location.fleets[owner.getId()].add(this);
		orderToAttackMove(builder.location); //this call does not go via the game Order handling system.  all computers should issue these orders on their own.
	}
	
	//updates the ship an increment towards time t - moving and attacking.  return value is meaningless/ignored
	@Override
	public boolean update(long t, Fleet.ShipIterator shipIteration)
	{
		flying_part.moveIncrement(t);
		MODES orig_mode;
		
		if (mode == MODES.IN_WARP)
			throw new IllegalStateException("shouldn't call update() if ship is already in warp");
		
		/* this do-while is necessary because it lets the ship go through
		 * multiple states within a time grain */
		do {
			orig_mode = mode;
			
			switch(mode)
			{
				/*TODO: change attack if there are teams or other factors*/
				case IDLE:
					SecondDest=null;
					Ship targettoattack = identifyClosestEnemy();
					if(targettoattack != null){
						setOtherDest(t, flying_part.getDestination());
						setupAttack(t, targettoattack);
						mode = MODES.ATTACKING;
					} 
					break;
				case MOVING:
					SecondDest = null;
					AbstractDestination<?> destination = flying_part.getDestination();
					if((!(destination instanceof Ship)) && flying_part.reachedDest()){
						if(destination instanceof Satellite<?>)
							{mode = MODES.ORBITING;}
						else mode = MODES.IDLE;
					}
					break;
				case ORBITING:
					SecondDest=null;
					break;
				case PROTECTORBIT:
					SecondDest=null;
					Ship possibletarget = identifyClosestEnemy();
					if (possibletarget != null) {
						setOtherDest(t, flying_part.getDestination());
						setupAttack(t, possibletarget);
						mode = MODES.ATTACKING;
					} 
					break;
				case TARGET_LOST:
					if (SecondDest!=null) {
						AIMove(SecondDest);
						SecondDest=null;
						mode = MODES.ATTACKMOVE;
					}
					else {
						mode=MODES.IDLE;
					}
					was_target=null;
					break;
				case ATTACKING:
					shootMissile(t);
					break;
				case USERATTACKING:
					shootMissile(t);
					break;
				case ATTACKMOVE:
					Ship atkMoveTarget = identifyClosestEnemy();
					if (atkMoveTarget != null){
						setOtherDest(t, flying_part.getDestination());
						setupAttack(t, atkMoveTarget);
						mode = MODES.ATTACKING;
						break;
					} 
					if (flying_part.reachedDest()){
						if(flying_part.getDestination() instanceof OwnableSatellite<?>) {
							mode = MODES.PROTECTORBIT;
						}
						else
							mode = MODES.IDLE;
					}
					break;
				case USERATTACKMOVE:
					Ship useratkMoveTarget = identifyClosestEnemy();
					if (useratkMoveTarget != null){
						setOtherDest(t, flying_part.getDestination());
						setupAttack(t, useratkMoveTarget);
						mode = MODES.ATTACKING;
						break;
					} 
					if (flying_part.reachedDest()){
						if(flying_part.getDestination() instanceof OwnableSatellite<?>) {
							mode = MODES.PROTECTORBIT;
						}
						else
							mode = MODES.IDLE;
					}
					break;
				case TRAVEL_TO_WARP:
					if(isClearToWarp())
					{
						System.out.println("clear to warp!");
						mode=MODES.ENTER_WARP;
						flying_part.setCurrent_flying_AI(new SpeedUpAI());
					}
					break;
				case ENTER_WARP:
					if(fastEnoughToWarp())
						engageWarpDrive(t, shipIteration);
					break;
				case EXIT_WARP:
					if(flying_part.getSpeed() <= type.max_speed)
						mode=MODES.ATTACKMOVE;
					break;
				case PICKUP_TROOPS:
					if(!doTransferTroops() || soldier >= type.soldier_capacity)
						mode=MODES.ORBITING;
					break;
				case IN_WARP:
					break; // nothing more to do.
			}
		} while (mode != orig_mode);
		
		return false;
	}
	
	private void setOtherDest(long t, AbstractDestination<?> d)
	{
		if(d instanceof Flyer<?,?,?>)
			SecondDest = new DestinationPoint(d.getXCoord(), d.getYCoord());
		else
			SecondDest = d;
	}
	
	public Ship identifyClosestEnemy(){
		Ship currentShip=null;
		Ship closestShip=null;
		double closestDistance,currentDistance;
		closestDistance = -1;
		for(int i = 0; i < Constants.MAX_PLAYERS; i++){
			if(i != owner.getId()){
				for(Fleet.ShipIterator j=location.fleets[i].iterator();j.hasNext();){
					currentShip = location.fleets[i].ships.get(j.next());
					if(currentShip!=null)
					{
						currentDistance = flying_part.findSqDistance(currentShip);
						if(currentDistance < Constants.Detection_Range_Sq){
							if(closestShip==null||closestDistance>currentDistance){
									closestShip = currentShip;
									closestDistance=currentDistance;
							}
						}
					}
				}	
			}	
		}
	return closestShip;
	}
	
	public void setupAttack(long t, Targetable<?> tgt){
		target= tgt;
		flying_part.setDestination(tgt);
		target.addAggressor(this);
		nextAttackingtime = t + Constants.Attacking_cooldown;
		
		//TODO: get constant 5 out of here
		flying_part.setCurrent_flying_AI(
			new TrackingAI(this.flying_part, Constants.Attacking_Range-5, TrackingAI.IN_RANGE_BEHAVIOR.STOP)
		);
	}
	
	public void userOverride(){
		SecondDest = null;
		if(target != null)
		{
			target.removeAggressor(this);
			target=null;
		}
	}
	
	//this function is called when time is rolled back to before the ship existed
	@Override
	public void removeFromGame()
	{
		location.fleets[owner.getId()].remove(this);
	}
	
	//this function is NOT incremental, i.e. it is only called once during updateGame() - before the updateGame function cycles through the systems
	//this function returns true if the ship exits warp, false otherwise.
	public boolean moveDuringWarp(long t, Iterator<Ship> ship_it)
	{
		//System.out.println("move during warp");
		if(mode == MODES.IN_WARP)
		{
			//System.out.println("...warping...");
			if(t >= arrival_time)
			{
				//System.out.println("arriving...")
				disengageWarpDrive(ship_it);
				return true;
			}
			else
			{
				double dist_moved = type.warp_speed*Constants.TIME_GRANULARITY;
				setWarp_x(getWarp_x() + dist_moved*exit_vec_x);
				setWarp_y(getWarp_y() + dist_moved*exit_vec_y);
			}
		}
		return false;
	}
	
	private boolean fastEnoughToWarp()
	{
		return (flying_part.getSpeed() >= Constants.WARP_EXIT_SPEED);
	}
	
	@Override
	public boolean isInWarpTransition() {
		return mode == MODES.ENTER_WARP || mode == MODES.EXIT_WARP;
	}
	
	protected boolean enforceSpeedCap()
	{
		return (mode != MODES.ENTER_WARP); //only enforce if mode is NOT enter warp, i.e. ships can go superspeed when warping
	}
	
	public void orderToMove(AbstractDestination<?> d)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP) //to ensure if the interface tries to issue an order, it can't
		{
			flying_part.setDestination(d);
			
			userOverride();
			
			//System.out.println(Integer.toString(id) + " orderToMove: t is " + Long.toString(t) + " and time is " + Long.toString(time));
			flying_part.setCurrent_flying_AI(
				new TrackingAI(this.flying_part, Constants.LANDING_RANGE, TrackingAI.IN_RANGE_BEHAVIOR.MATCH_SPEED)
			);
			mode=MODES.MOVING;
		}
	}
	
	public void AIMove(AbstractDestination<?> d)
	{
		flying_part.setDestination(d);
		flying_part.setCurrent_flying_AI(
			new TrackingAI(this.flying_part, Constants.LANDING_RANGE, TrackingAI.IN_RANGE_BEHAVIOR.MATCH_SPEED)
		);
	}
	
	public void orderToAttackMove(AbstractDestination<?> d)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP)
		{
			flying_part.setDestination(d);
			userOverride();
			
			flying_part.setCurrent_flying_AI(
				new TrackingAI(this.flying_part, Constants.LANDING_RANGE, TrackingAI.IN_RANGE_BEHAVIOR.MATCH_SPEED)
			);
			mode = MODES.USERATTACKMOVE;
		}
	}
	
	public void orderToAttack(long t, Targetable<?> tgt)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP)
		{
			//System.out.println(Integer.toString(id) + "orderToAttack: t is " + Long.toString(t));
			userOverride();
			mode=MODES.USERATTACKING;
			setupAttack(t, tgt);
		}
	}
	
	public void orderToWarp(GSystem sys)
	{
		if(mode != MODES.EXIT_WARP && mode != MODES.IN_WARP && mode != MODES.ENTER_WARP && sys != location) //if sys == location, user means to cancel warp command.
		{
			userOverride();
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
			flying_part.setCurrent_flying_AI(new WarpAI(this));
		}
	}
	
	public void orderToInvade(OwnableSatellite<?> sat, long t)
	{
		if(mode == MODES.ORBITING|| mode==MODES.PROTECTORBIT || mode==MODES.MOVING  || mode == MODES.ATTACKING) //TODO: what modes is this valid in?
		{
			if(flying_part.findSqDistance(sat) < Constants.LANDING_RANGE*Constants.LANDING_RANGE)
			{
				if(sat.getOwner() != null)
				{
					synchronized(sat.facilities)
					{
						if(sat.the_base == null) //if base isn't finished being built, player can take over without a fight
							sat.changeOwnerAtTime(getOwner(), t);
						else
							sat.the_base.attackedByTroops(t, this);
					}
				}
				else
				{
					((OwnableSatellite<?>)flying_part.getDestination()).changeOwnerAtTime(getOwner(), t);
				}
			}
		}
	}
	
	public void orderToPickupTroops(long t) {
		
		if((mode == MODES.ORBITING||mode == MODES.PROTECTORBIT )&&
				flying_part.getDestination() instanceof OwnableSatellite<?> &&
				((OwnableSatellite<?>)flying_part.getDestination()).getOwner() == owner &&
				((OwnableSatellite<?>)flying_part.getDestination()).the_base != null &&
				soldier < type.soldier_capacity)
		{
			mode = MODES.PICKUP_TROOPS;
		}
	}
	
	//returns true for success, false if the destination is no longer owned by the player or the base has been destroyed
	private boolean doTransferTroops()
	{
		OwnableSatellite<?> sat = (OwnableSatellite<?>)flying_part.getDestination();
		if(sat.getOwner() == owner && sat.the_base != null)
		{
			if(flying_part.findSqDistance(flying_part.getDestination()) <= Constants.LANDING_RANGE*Constants.LANDING_RANGE)
			{
				float get_soldiers = Math.min(type.soldier_capacity - soldier, Constants.troop_transfer_rate*Constants.TIME_GRANULARITY);
				synchronized(sat.facilities)
				{
					soldier += sat.the_base.retrieveSoldiers(get_soldiers);
				}
			}
			return true;
		}		
		else
			return false;
	}
	
	public int warpRange(){return type.warp_range;}
	
	private void engageWarpDrive(long time, Fleet.ShipIterator shipIteration)
	{
		//This function works very much like the destroyed() function
		System.out.println("Engaging warp drive....");
		
		//remove from listing in system
		if(shipIteration != null)
			shipIteration.remove(); //remove via the iterator to avoid ConcurrentModificationException
		else
			location.fleets[owner.getId()].remove(this);
		
		//notify aggressors
		for(Targetter<?> t : aggressors)
			t.targetHasWarped(time);
		
		aggressors.clear();
		
		//deselect the ship, if it was selected
		SwingUtilities.invokeLater(new ShipDeselector(this));
		
		// TODO this is a huge hack, needs to die
		//compute details of flight plan
		setWarp_x(location.x);
		setWarp_y(location.y);
		arrival_time = time + (long)(exit_vec_len/type.warp_speed);
		System.out.println("arrival time is " + Long.toString(arrival_time));
		
		mode=MODES.IN_WARP;
		owner.getShips_in_transit().add(this);
	}
	
	private void disengageWarpDrive(Iterator<Ship> ship_it)
	{
		System.out.println("Disengaging warp drive");
		location=warp_destination;
		
		//set up random jitter with arrival_time as seed.  necessary to seed it to keep everything coordinated.
		Random generator = new Random(arrival_time);
		
		//rewrite physics values
		double x=-exit_vec_x*ESCAPE_DIST*EXIT_MULTIPLIER + location.absoluteCurX() + EXIT_PLACE_JITTER*generator.nextGaussian();
		double y=-exit_vec_y*ESCAPE_DIST*EXIT_MULTIPLIER + location.absoluteCurY() + EXIT_PLACE_JITTER*generator.nextGaussian();
		double speed = Constants.WARP_EXIT_SPEED;
		double direction = exit_direction + EXIT_DIRECTION_JITTER*generator.nextGaussian(); //should already be true, but just in case
		flying_part.setInitialPositionAndVelocity(x, y, speed, direction);
		
		mode=MODES.EXIT_WARP;
		double time = (1.125*speed)/getAccel();
		flying_part.setDestination(new DestinationPoint(x+.5*speed*time*Math.cos(direction), y+.5*speed*time*Math.sin(direction)));
		flying_part.setCurrent_flying_AI(new StopAI());
		
		ship_it.remove();//owner.ships_in_transit.remove(this);
		location.fleets[owner.getId()].add(this);
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
		// This is somewhat sketchy since it's a floating point equality test.
		// Since our AI adjusts until the values are exactly equal, this hasn't
		// caused any problems yet.
		if(flying_part.getDirection() != exit_direction)
			return false;
		
		double radial_vec_x = flying_part.getPos_x()-location.absoluteCurX();
		double radial_vec_y = flying_part.getPos_y()-location.absoluteCurY();
		
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
	
	public void shootMissile(long t){
		if (flying_part.findSqDistance(target) < Constants.Attacking_Range_Sq &&(nextAttackingtime<=t))
		{
			Missile m = new Missile(this, t, target); 
			location.missiles.put(m.id, m);
			nextAttackingtime= t + Constants.Attacking_cooldown;
		}
	}
	
	@Override
	public void destroyed(long time)
	{
		//System.out.println("destroyed-before");	
		if(location.fleets[owner.getId()].remove(this))//if is so in case another attack has already destroyed the ship, but both call the destroyed method
		{
			is_alive=false;
			
			//notify aggressors
			for(Targetter<?> t : aggressors)
				t.targetIsDestroyed(time);
			
			if(target != null)
				target.removeAggressor(this);
			
			//notify interface
			SwingUtilities.invokeLater(new ShipDeselector(this));
		
			//System.out.println("destroyed-after");
		}
	}
	
	@Override
	public void targetIsDestroyed(long t){targetIsDestroyed(t, false, null);}
	
	public void targetIsDestroyed(long t, boolean late_order, Targetable<?> tgt)
	{
		targetLost(t, LOST_REASON.DESTROYED, late_order, tgt);
	}
	
	@Override
	public void targetHasWarped(long t){targetHasWarped(t, false, null);}
	
	public void targetHasWarped(long t, boolean late_order, Targetable<?> tgt)
	{
		targetLost(t, LOST_REASON.WARPED, late_order, tgt);
	}
	
	private void targetLost(long t, LOST_REASON reason, boolean late_order, Targetable<?> tgt /*ignored if late_order is false*/)
	{
		//System.out.println("target lost");
		if (flying_part.getDestination()==(AbstractDestination<?>)target && (mode==MODES.ATTACKING||mode==MODES.USERATTACKING))
		{
			//System.out.println("\tchanging destination...");
			//Need to look backwards a time grain because otherwise we will get DataNotYetSavedException
			//since missile detonation code runs before saveAllData().
			//It is possible that we could write functions to get the lastest position that would be safe,
			//but at the moment they don't exist.
			
			flying_part.setDestination(new DestinationPoint(
							target.getXCoord(),
							target.getYCoord()
						));
			SwingUtilities.invokeLater(new DestUpdater(this));
		}
		
		mode = MODES.TARGET_LOST;
		if(!late_order)
			was_target = target;
		else
			was_target = tgt;
		
		target=null;
		
		//TODO: player notification - THIS SHOULD USE LOST_REASON
	}
	
	private static class DestUpdater implements Runnable
	{
		final Ship the_ship;
		
		private DestUpdater(Ship s)
		{
			the_ship=s;
		}
		
		public void run()
		{
			if(GameInterface.GC.GI != null && GameInterface.GC.GI.ShipPanel.getShip() == the_ship)
			{
				GameInterface.GC.GI.ShipPanel.updateDestDisplay(the_ship.flying_part.getDestination());
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
	public AbstractDestination<?> getSecondDest(){return SecondDest;}
	public void setSecondDest(AbstractDestination<?> dest){SecondDest=dest;}
	
	public float getSoldier() {return soldier;}
	public MODES getMode(){return mode;}
	public void setMode(MODES m){mode=m;}
	public void setExit_vec_x(double x){exit_vec_x = x;}
	public double getExit_vec_x(){return exit_vec_x;}
	public void setExit_vec_y(double y){exit_vec_y = y;}
	public double getExit_vec_y(){return exit_vec_y;}
	public void setExit_direction(double d){exit_direction=d;}
	public double getExit_direction(){return exit_direction;}
	
	public long getNextAttackingtime(){return nextAttackingtime;}
	public void setNextAttackingtime(long t){nextAttackingtime = t;}
	public int getNext_missile_id(){return next_missile_id;}
	public void setNext_missile_id(int id){next_missile_id = id;}
	
	//support for Selectable
	@Override
	public ImageResource getImage()
	{
		return type.img;
	}
	
	public static class ShipId extends Flyer.FlyerId<ShipId> implements Comparable<ShipId>
	{
		Shipyard manufacturer;
		int queue_id;
		
		public ShipId(int q_id, Shipyard manu)
		{
			manufacturer = manu;
			queue_id = q_id;
		}
		
		public ShipId(){};
		
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

		@Override
		public int compareTo(ShipId o) {
			if (manufacturer != null && o.manufacturer != null) {
				int manufacturer_comp = manufacturer.compareTo(o.manufacturer);
				if (manufacturer_comp != 0)
					return manufacturer_comp;
			}
			// the XmlEncoder from java beans will end up calling this with
			// partially-instantiated objects by recursing to instantiate a
			// manufacturer (shipyard) and eventually ending up back at the
			// same object in the object graph; this makes us at least try
			// to do something sane in these cases.
			else if (manufacturer == null && o.manufacturer != null) {
				return -1;
			}
			else if (o.manufacturer == null && manufacturer != null){
				return 1;
			}
			
			if (queue_id < o.queue_id)
				return -1;
			else if (queue_id == o.queue_id)
				return 0;
			else
				return 1;
		}

		public void setManufacturer(Shipyard m) {this.manufacturer = m;}
		public Shipyard getManufacturer() {return manufacturer;}
		public void setQueue_id(int queue_id) {this.queue_id = queue_id;}
		public int getQueue_id() {return queue_id;}
	}

	@Override
	public boolean shouldSelect(double x, double y, double tolerance) {
		double sx = flying_part.getPos_x();
		double sy = flying_part.getPos_y();
		
		return sx-tolerance-type.getDim()*type.img.scale/2 <= x &&
		x <= sx + tolerance + type.getDim()*type.img.scale/2 &&
		sy - tolerance - type.getDim()*type.img.scale/2 <= y &&
		y <= sy + tolerance + type.getDim()*type.img.scale/2;
	}

	public AbstractDestination<?> getDestination() {
		return flying_part.getDestination();
	}

	public double getWarp_x() {
		return warp_x;
	}

	public void setWarp_x(double warp_x) {
		this.warp_x = warp_x;
	}

	public double getWarp_y() {
		return warp_y;
	}

	public void setWarp_y(double warp_y) {
		this.warp_y = warp_y;
	}
}