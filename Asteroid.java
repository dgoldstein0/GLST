public class Asteroid extends Satellite
{
	String name;
	
	public Asteroid(String nm)
	{
		name=nm;
	}
	
	public Asteroid(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
}