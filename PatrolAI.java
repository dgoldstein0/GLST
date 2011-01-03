public strictfp class PatrolAI extends FlyerAI
{
	//this AI makes a ship fly in a circle
	
	double circle_patrol_speed;
	int patrol_direction; //clockwise or counterclockwise
		final static int CLOCKWISE = 1;
		final static int COUNTERCLOCKWISE = 1;
	
	int state;
		final static int TRAVEL_TO_PATROL = 0;
		final static int PATROLLING = 1;
	
	TrackingAI tracker;
	
	//TRAVEL_TO_PATROL state
	//the point the ship should fly to to begin the patrol
	double patrol_start_x;
	double patrol_start_y;
	double patrol_start_ang;
		final static double IN_RANGE = 6.0; //how close the ship should get to the correct start position
	
	//PATROL state
	//the constant speed and angle change to maintain during patrol
	double patrol_ang_chng;
	double patrol_speed;
	
	public PatrolAI(Flyer<?,?,?> f, double x, double y, double radius, int dir)
	{
		the_flyer=f;
		state = TRAVEL_TO_PATROL;
		patrol_direction = dir;
		
		//decide how to go to patrol position
		double radial_x = f.getPos_x()-x;
		double radial_y = f.getPos_y()-y;
		
		patrol_start_x = radial_x*radius/Math.hypot(radial_x, radial_y) + x;
		patrol_start_y = radial_y*radius/Math.hypot(radial_x, radial_y) + y;
		
		tracker = new TrackingAI(f, 5.0d);
		f.destination = new DestinationPoint(patrol_start_x, patrol_start_y);
		
		patrol_start_ang = Math.atan2(radial_x,-radial_y)*dir; //using (-y,x) is the same as rotating 90 degrees clockwise
		
		//decide how to patrol
		patrol_ang_chng = the_flyer.type.max_angular_vel;
		patrol_speed = radius*patrol_ang_chng;
		if(patrol_speed > the_flyer.type.max_speed)
		{
			patrol_speed = the_flyer.type.max_speed;
			patrol_ang_chng = patrol_speed/radius;
		}
		patrol_ang_chng *= GalacticStrategyConstants.TIME_GRANULARITY*dir;
	}
	
	public double calcDesiredDirection()
	{
		switch(state)
		{
			case TRAVEL_TO_PATROL:
				double dif_x = the_flyer.getPos_x()-patrol_start_x;
				double dif_y = the_flyer.getPos_y()-patrol_start_y;
				
				if(dif_x*dif_x + dif_y*dif_y > IN_RANGE * IN_RANGE)
					return tracker.calcDesiredDirection();
				else
				{
					double chng = patrol_start_ang - the_flyer.direction;
					if(chng > Math.PI)
						chng -= 2*Math.PI;
					else if(chng < -Math.PI)
						chng += 2*Math.PI;
					
					if(chng == 0)
					{
						state=PATROLLING;
						return calcDesiredDirection();
					}
					else
						return chng;
				}
				//break;
			case PATROLLING:
				return patrol_ang_chng;
				//break;
			default:
				return 0.0;
		}
	}
	
	public double calcDesiredSpeed(double dir)
	{
		switch(state)
		{
			case TRAVEL_TO_PATROL:
				return tracker.calcDesiredSpeed(dir);
				//break;
			case PATROLLING:
				return patrol_speed;
				//break;
			default:
				return 0.0;
		}
	}
}