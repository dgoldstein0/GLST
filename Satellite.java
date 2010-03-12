import java.util.*;

public abstract class Satellite extends StellarObject implements Positioning, Destination
{
	Orbit orbit;
	String name;
	int id;
	
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
	
	public void setId(int i){id=i;}
	public int getId(){return id;}	
	
	//the rest of the code is to implement Destination without unnecessary computation.
	
	public double getXCoord(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return x_coord;
	}
	
	public double getYCoord(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return y_coord;
	}
	
	public void computeCoords(long t)
	{
		//save current position
		double x = orbit.cur_x;
		double y = orbit.cur_y;
		double v_x = orbit.vel_x;
		double v_y = orbit.vel_y;
		
		orbit.move(t);
		double boss_y, boss_x;
		if(orbit.boss instanceof Satellite)
		{
			((Satellite)orbit.boss).computeCoords(t);
			boss_y = ((Satellite)orbit.boss).getYCoord(t);
			boss_x = ((Satellite)orbit.boss).getXCoord(t);
		}
		else //boss is a GSystem
		{
			boss_x = orbit.boss.absoluteCurX();
			boss_y = orbit.boss.absoluteCurY();
		}
		x_coord = orbit.cur_x + boss_x;
		y_coord = orbit.cur_y + boss_y;
		
		//restore current position
		orbit.cur_x = x;
		orbit.cur_y = y;
		orbit.vel_x = v_x;
		orbit.vel_y = v_y;
		
		last_t_gotten=t;
	}
	
	long last_t_gotten=0;
	double x_coord;
	double y_coord;
}