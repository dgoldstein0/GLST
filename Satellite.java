import java.util.*;

public abstract class Satellite extends StellarObject implements Positioning
{
	Orbit orbit;
	byte habitability;
	long population;
	
	public HashSet<Double> getMassSet()
	{
		HashSet<Double> mass_set=new HashSet<Double>();
		mass_set.add(mass);
		return mass_set;
	}
	
	public Orbit getOrbit(){return orbit;}
	public void setOrbit(Orbit o){orbit=o;}
	public byte getHabitability(){return habitability;}
	public void setHabitability(byte habit){habitability=habit;}
	public long getPopulation(){return population;}
	public void setPopulation(long pop){population=pop;}
	
	public int absoluteCurX(){return orbit.absoluteCurX();}
	public int absoluteCurY(){return orbit.absoluteCurY();}
	public int absoluteInitX(){return orbit.absoluteInitX();}
	public int absoluteInitY(){return orbit.absoluteInitY();}
}