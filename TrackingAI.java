public class TrackingAI extends FlyerAI
{
	double dest_tolerance; //how close we should be to destination before match speed by default
	
	public TrackingAI(Flyer f, double tol)
	{
		the_flyer = f;
		dest_tolerance = tol;
	}
	
	public double calcDesiredDirection()
	{
		double dest_vec_x = the_flyer.destinationX() - the_flyer.pos_x;
		double dest_vec_y = the_flyer.destinationY() - the_flyer.pos_y;
		double desired_change = Math.atan2(dest_vec_y, dest_vec_x)-the_flyer.direction;

		if(desired_change > Math.PI)
			desired_change -= 2*Math.PI;
		else if(desired_change < -Math.PI)
			desired_change += 2*Math.PI;
		
		return desired_change;
	}
	
	public double calcDesiredSpeed(double desired_direction)
	{
		//if close to dest
		double match_speed = Math.hypot(the_flyer.destinationVelX(),the_flyer.destinationVelY())*Math.cos(desired_direction)*Math.abs(Math.cos(desired_direction));
		double time_to_chng = (the_flyer.speed-match_speed)/(the_flyer.type.accel_rate);
		double time_to_dest = Math.hypot(the_flyer.pos_x - the_flyer.destinationX(),the_flyer.pos_y - the_flyer.destinationY())/the_flyer.speed;
		
		if(Math.hypot(the_flyer.pos_x - the_flyer.destinationX(),the_flyer.pos_y - the_flyer.destinationY()) < dest_tolerance || time_to_chng > time_to_dest)
		{
			//System.out.println("match speed: " + Double.toString(match_speed));
			return match_speed;
		}
		else if(desired_direction < Math.PI/2.0 && desired_direction > -Math.PI/2.0) //else if destination is forward
		{
			//System.out.println("full speed");
			return the_flyer.type.max_speed*Math.cos(desired_direction)*Math.cos(desired_direction);
		}
		else //destination is backward, stop to turn around
		{
			//System.out.println("stop!");
			return 0.0d;
		}
	}
}