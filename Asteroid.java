public class Asteroid extends Satellite
{
	public Asteroid(int i, String nm) //name should not be null.  If the asteroid does not yet have a name, nm should be empty string
	{
		id=i;
		name=nm;
	}
	
	public Asteroid(){}
}