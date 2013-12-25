package galactic_strategy.game_objects;

import galactic_strategy.Player;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.FacilityDescriber;
import galactic_strategy.sync_engine.Saveable;

import java.util.HashSet;

//T = the class which extends Facility
public strictfp abstract class Facility<T extends Facility<T>> implements Targetable<T>, Saveable<T>, Comparable<T>
{	
	OwnableSatellite<?> location;
	int id;
	
	HashSet<Targetter<?>> aggressors;
	int endurance;
	int damage;
	boolean is_alive;
	
	public DataSaverControl<T> data_control;
	
	public Facility(OwnableSatellite<?> l, int i, int endu)
	{
		location=l;
		
		id=i;
		
		endurance=endu;
		damage=0;
		is_alive=true;
		aggressors = new HashSet<Targetter<?>>();
		data_control = new DataSaverControl<T>((T) this);
	}
	
	@Override
	public void addDamage(long t, int d)
	{
		damage+=d;
		if(damage>=endurance)
			destroyed(t);
	}
	
	@Override
	public void destroyed(long t) //default option.  Base, Mine and TaxOffice override
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
		}
	}
	
	public FacilityDescriber<T> describer()
	{
		return new FacilityDescriber<T>((Facility<T>)this);
	}
	
	public void removeFromGame()
	{
		synchronized(location.facilities)
		{
			//should probably cache the facility for recall... but anyway
			location.facilities.remove(id);
		}
	}
	
	@Override
	public boolean isAlive(){return is_alive;}

	public abstract void ownerChanged(long t);
	
	
	@Override //for Saveable
	public DataSaverControl<T> getDataControl(){return data_control;}
	
	public int compareTo(T f)
	{
		if (f == null)
			return 1;
		
		if (location == null && f.location != null)
			return -1;
		else if (location != null && f.location == null)
			return 1;
		else if (location != null && f.location != null)
		{
			int location_compare = location.compareTo(f.location);
			if (location_compare != 0)
				return location_compare;
		}
		
		if (id < f.id)
			return -1;
		else if (id == f.id)
			return 0;
		else
			return 1;
	}
	public Player getOwner() {
		return location.getOwner();
	}
	
	public Facility(){}
	
	@Override public double getXCoord(){return location.getXCoord();}
	@Override public double getYCoord(){return location.getYCoord();}
	@Override public double getXVel(){return location.getXVel();}
	@Override public double getYVel(){return location.getYVel();}
	
	@Override public HashSet<Targetter<?>> getAggressors(){return aggressors;}
	
	@Override public void addAggressor(Targetter<?> t)
	{
		aggressors.add(t);
	}
	
	@Override public void removeAggressor(Targetter<?> t)
	{
		aggressors.remove(t);
	}
	public abstract void updateStatus(long t);
	public final String getName(){return getType().name;}
	public abstract FacilityType getType();
	public final String imageLoc(){return getType().image_path;}
	
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public int getEndurance(){return endurance;}
	public void setEndurance(int e){endurance=e;}
	public OwnableSatellite<?> getLocation(){return location;}
	public void setLocation(OwnableSatellite<?> s){location=s;}
	
	public int getId(){return id;}
	public void setId(int i){id=i;}
}