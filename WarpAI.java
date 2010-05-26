public class WarpAI extends FlyerAI
{
	Flyer the_flyer;
	
	public WarpAI(Flyer f)
	{
		the_flyer=f;
	}
	
	public double calcDesiredDirectionChng()
	{
		return ((Ship)the_flyer).exit_direction-the_flyer.direction;
	}
	
	public double calcDesiredSpeed(double angle_chng)
	{
		double cosine = Math.cos(angle_chng);
		return the_flyer.type.max_speed*cosine*Math.abs(cosine);
	}
	
	public Flyer getThe_flyer(){return the_flyer;}
	public void setThe_flyer(Flyer f){the_flyer=f;}
}