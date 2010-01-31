import java.util.*;

public abstract class Satellite extends StellarObject implements Positioning
{
	Orbit orbit;
	byte habitability;
	long population;
	String name;
	
	/*public HashSet<Double> getMassSet()
	{
		HashSet<Double> mass_set=new HashSet<Double>();
		mass_set.add(mass);
		return mass_set;
	}*/
	
	public double massSum()
	{
		return mass;
	}
	
	public Orbit getOrbit(){return orbit;}
	public void setOrbit(Orbit o){orbit=o;}
	public byte getHabitability(){return habitability;}
	public void setHabitability(byte habit){habitability=habit;}
	public long getPopulation(){return population;}
	public void setPopulation(long pop){population=pop;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	
	public double absoluteCurX(){return orbit.absoluteCurX();}
	public double absoluteCurY(){return orbit.absoluteCurY();}
	public double absoluteInitX(){return orbit.absoluteInitX();}
	public double absoluteInitY(){return orbit.absoluteInitY();}
}