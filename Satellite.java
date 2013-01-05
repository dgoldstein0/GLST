public strictfp abstract class Satellite<T extends Satellite<T>> extends StellarObject implements Destination<T>
{
	Orbit orbit;
	int id;
	
	public double massSum()
	{
		return mass;
	}
	
	public Describer<T> describer(){return new SatelliteDescriber<T>((Satellite<T>)this);} //this needs a reminder that it is a Satellite<T>
	
	public Orbit getOrbit(){return orbit;}
	public void setOrbit(Orbit o){orbit=o;}
	
	//for Selectable, which is implemented by StellarObject
	@Override
	public String generateName(){return getName();}
	@Override
	public int getSelectType(){return Selectable.SATELLITE;}
	
	public double absoluteCurX(){return orbit.absoluteCurX();}
	public double absoluteCurY(){return orbit.absoluteCurY();}
	public double absoluteInitX(){return orbit.absoluteInitX();}
	public double absoluteInitY(){return orbit.absoluteInitY();}
	public double getAbsVelX(){return orbit.getAbsVelX();}
	public double getAbsVelY(){return orbit.getAbsVelY();}
	
	public void setId(int i){id=i;}
	public int getId(){return id;}	
	
	//the rest of the code is to implement Destination without unnecessary computation.
	@Override
	public double getXCoord(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return x_coord;
	}
	@Override
	public double getYCoord(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return y_coord;
	}
	@Override
	public double getXVel(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return x_vel;
	}
	@Override
	public double getYVel(long t)
	{
		if(t != last_t_gotten)
			computeCoords(t);
		
		return y_vel;
	}	
	
	public void computeCoords(long t)
	{
		//save current position
		double x = orbit.cur_x;
		double y = orbit.cur_y;
		double v_x = orbit.vel_x;
		double v_y = orbit.vel_y;
		
		orbit.move(t);
		double boss_y, boss_x, boss_vx, boss_vy;
		if(orbit.boss instanceof Satellite<?>)
		{
			Satellite<?> sboss = ((Satellite<?>)orbit.boss);

			boss_y = sboss.getYCoord(t);
			boss_x = sboss.getXCoord(t);
			boss_vy = sboss.getYVel(t);
			boss_vx = sboss.getXVel(t);
		}
		else //boss is a GSystem
		{
			boss_x = orbit.boss.absoluteCurX();
			boss_y = orbit.boss.absoluteCurY();
			boss_vx = 0.0d;
			boss_vy = 0.0d;
		}
		x_coord = orbit.cur_x + boss_x;
		y_coord = orbit.cur_y + boss_y;
		x_vel = orbit.vel_x + boss_vx;
		y_vel = orbit.vel_y + boss_vy;
		
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
	double x_vel;
	double y_vel;

	public abstract void recursiveSaveData();
	public abstract void recursiveRevert(long t) throws DataSaverControl.DataNotYetSavedException;
}