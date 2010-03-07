import java.util.*;

public abstract class Satellite extends StellarObject implements Positioning
{
	Orbit orbit;
	String name;
	
	public double massSum()
	{
		return mass;
	}
	
	public Orbit getOrbit(){return orbit;}
	public void setOrbit(Orbit o){orbit=o;}

	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	
	public double absoluteCurX(){return orbit.absoluteCurX();}
	public double absoluteCurY(){return orbit.absoluteCurY();}
	public double absoluteInitX(){return orbit.absoluteInitX();}
	public double absoluteInitY(){return orbit.absoluteInitY();}
	public double getAbsVelX(){return orbit.getAbsVelX();}
	public double getAbsVelY(){return orbit.getAbsVelY();}
}