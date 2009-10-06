import java.util.*;

public class Moon extends Satellite
{
	String name;
	HashSet<? super Station> satellites;
	
	public Moon(double m, String name)
	{
		this.mass=m;
		this.name=name;
	}
	
	public Moon(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public HashSet getSatellites(){return satellites;}
	public void setSatellites(HashSet<? super Station> sat){satellites=sat;}
}