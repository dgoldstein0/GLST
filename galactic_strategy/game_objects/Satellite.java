package galactic_strategy.game_objects;

import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Describer;
import galactic_strategy.sync_engine.SatelliteDescriber;
import galactic_strategy.ui.Selectable;

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
	
	//the rest of the code is to implement Destination
	@Override
	public double getXCoord()
	{
		return orbit.absoluteCurX();
	}
	@Override
	public double getYCoord()
	{
		return orbit.absoluteCurY();
	}
	@Override
	public double getXVel()
	{
		return orbit.getAbsVelX();
	}
	@Override
	public double getYVel()
	{
		return orbit.getAbsVelY();
	}

	@Override
	public double getX() {
		return getXCoord();
	}
	
	@Override
	public double getY() {
		return getYCoord();
	}
	
	public abstract void recursiveSaveData(long time);
	public abstract void recursiveRevert(long t) throws DataSaverControl.DataNotYetSavedException;

}