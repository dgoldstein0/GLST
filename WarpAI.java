public class WarpAI extends FlyerAI
{
	Flyer the_flyer;
	
	public WarpAI(Flyer f)
	{
		the_flyer=f;
	}
	
	public double calcDesiredDirection()
	{
		return ((Ship)the_flyer).exit_direction;
	}
	
	public int directionType()
	{
		return FlyerAI.ABS_DIRECTION;
	}
	
	public double calcDesiredSpeed(double angle_chng)
	{
		double cosine = Math.cos(angle_chng);
		return the_flyer.type.max_speed*cosine*Math.abs(cosine);
	}
	
	public Flyer getThe_flyer(){return the_flyer;}
	public void setThe_flyer(Flyer f){the_flyer=f;}
}