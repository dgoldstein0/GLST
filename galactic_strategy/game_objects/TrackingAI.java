package galactic_strategy.game_objects;

public strictfp class TrackingAI extends FlyerAI
{
	double dest_tolerance; //how close we should be to destination before match speed by default
	IN_RANGE_BEHAVIOR in_range_behavior; //if the AI should try to match the speed of its destination or not
	
	public static enum IN_RANGE_BEHAVIOR {NO_SLOWDOWN, MATCH_SPEED, STOP};
	
	/**TrackingAI constructor.
	 *
	 * For following an object closely, like a planet, use low tol and
	 * 	ms=MATCH_SPEED.
	 * For attacking / following at a distance, use high tol and ms=MATCH_SPEED.
	 * Missiles use ms=NO_SLOWDOWN, so they don't try to match their target's
	 * 	maneuvers but rather will plow right into their targets.
	 * 
	 * @param f a reference to the Flyer (ship or missile) using this AI to
	 * 		direct is flight.
	 * @param tol the distance at which the Flyer is "close enough" to its
	 * 		destination and should just match its speed.
	 * @param ms whether the Flyer should try to match its destination's speed
	 * 		or not.
	 */
	public TrackingAI(FlyingThing f, double tol, IN_RANGE_BEHAVIOR ms)
	{
		super(f);
		dest_tolerance = tol;
		in_range_behavior = ms;
	}
	
	/**
	 * @return the angle through which the ship must rotate to be directly
	 * facing its destination
	 */
	public double calcDesiredDirection(long t)
	{
		double dest_vec_x = flying_thing.destination.getXCoord() - flying_thing.getPos_x();
		double dest_vec_y = flying_thing.destination.getYCoord() - flying_thing.getPos_y();
	
		return Math.atan2(dest_vec_y, dest_vec_x);
	}
	
	public int directionType()
	{
		return FlyerAI.ABS_DIRECTION;
	}
	
	public double calcDesiredSpeed(long t, double desired_direction) //the argument is the output of calcDesiredDirection()
	{
		double cos = Math.cos(desired_direction);
		
		// the speed the ship may want to match.  The cosines are used to slow down
		// the ship when it is not heading directly at its destination
		double speed_to_match = Math.hypot(
				flying_thing.destination.getXVel(),
				flying_thing.destination.getYVel()
			)*cos*Math.abs(cos);
			
		//the time it would take for the ship to match the speed of its target from its current speed
		double time_to_chng = (flying_thing.getSpeed()-speed_to_match)/flying_thing.getCapabilities().getAccel();
		
		//the time it would take for the ship to travel the remaining distance to its destination
		double time_to_dest = Math.hypot(
				flying_thing.getPos_x() - flying_thing.destination.getXCoord(),
				flying_thing.getPos_y() - flying_thing.destination.getYCoord()
			)/flying_thing.getSpeed();
		
		//The "if" here asks: should the Flyer should try to slow down to stop/match speed of destination AND...
			//is the flyer close enough to its destination that we can say it is there already? OR
			//(heuristic) is the time needed to match speed greater than time to arrival if traveling at constant speed (same as v^2 > a*d if the destination is not moving) 
		if(flying_thing.findSqDistance(flying_thing.destination) < dest_tolerance*dest_tolerance
			|| time_to_chng > time_to_dest)
		{
			switch(in_range_behavior)
			{
				case MATCH_SPEED:
					return speed_to_match;
				case STOP:
					return 0.0;
				case NO_SLOWDOWN:
					break;
			}
		}
		
		if(desired_direction < Math.PI/2.0 && desired_direction > -Math.PI/2.0) //else if destination is forward
		{
			//go a portion of the max speed - cosines used to slow ship when it isn't going straight
			return flying_thing.getCapabilities().getMaxSpeed()*cos*cos;
		}
		else //destination is backward, stop to turn around
			return 0.0d;
	}
	
	public TrackingAI(){}
	public double getDest_tolerance(){return dest_tolerance;}
	public void setDest_tolerance(double d){dest_tolerance = d;}
	public IN_RANGE_BEHAVIOR getIn_range_behavior(){return in_range_behavior;} 
	public void setIn_range_behavior(IN_RANGE_BEHAVIOR b){in_range_behavior = b;}
}