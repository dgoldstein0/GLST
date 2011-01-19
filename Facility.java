
import java.util.HashSet;

//T = the class which extends Facility
public strictfp abstract class Facility<T extends Facility<T>> implements Targetable<T>, RelaxedSaveable<T>
{	
	OwnableSatellite<?> location;
	int id;
	
	HashSet<Targetter<?>> aggressors;
	int endurance;
	int damage;
	boolean is_alive;
	
	long last_time;//the last time it was updated
	
	RelaxedDataSaverControl<T, ? extends FacilityDataSaver<T> > data_control; //must be instantiated by subclasses
	
	public Facility(OwnableSatellite<?> l, int i, long t, int endu)
	{
		location=l;
		
		id=i;
		
		last_time=t;
		
		endurance=endu;
		damage=0;
		is_alive=true;
		aggressors = new HashSet<Targetter<?>>();
	}
	
	public void addDamage(long t, int d)
	{
		updateStatus(t);
		damage+=d;
		if(damage>=endurance)
			destroyed();
		data_control.saveData();
	}
	
	public void destroyed() //default option.  Base overrides this
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
			decrementFacilityTracker();
		}
	}
	
	public FacilityDescriber<T> describer()
	{
		return new FacilityDescriber<T>((Facility<T>)this);
	}
	
	public void decrementFacilityTracker(){
		if(getType()==FacilityType.MINE){
			location.number_mines--;
		}
		if(getType()==FacilityType.TAXOFFICE){
			location.number_taxoffices--;
		}
		location.number_facilities--;
	}
	
	@Override
	public void handleDataNotSaved(long t){removeFromGame(t);}
	
	public void removeFromGame(long t)
	{
		synchronized(location.facilities)
		{
			//should probably cache the facility for recall... but anyway
			location.facilities.remove(id);
			decrementFacilityTracker();
		}
	}
	
	@Override
	public boolean isAlive(){return is_alive;}
	@Override
	public boolean isAliveAt(long t)
	{
		return data_control.saved_data[data_control.getIndexForTime(t)].alive;
	}
	
	//Most implementations should do last_time = time, though this is not strictly required.
	public abstract void ownerChanged(long t);
	
	
	@Override //for Saveable
	public RelaxedDataSaverControl<T, ? extends FacilityDataSaver<T> > getDataControl(){return data_control;}
	@Override
	public long getTime(){return last_time;}
	@Override
	public void setTime(long t){last_time=t;}
	
	public Facility(){}
	
	public long getLast_time(){return last_time;}
	public void setLast_time(long t){last_time=t;}
	
	@Override public double getXCoord(long t){return location.getXCoord(t);}
	@Override public double getYCoord(long t){return location.getYCoord(t);}
	@Override public double getXVel(long t){return location.getXVel(t);}
	@Override public double getYVel(long t){return location.getYVel(t);}
	
	@Override public HashSet<Targetter<?>> getAggressors(){return aggressors;}
	
	@Override public void addAggressor(Targetter<?> t)
	{
		aggressors.add(t);
		data_control.saveData();
	}
	
	@Override public void removeAggressor(Targetter<?> t)
	{
		aggressors.remove(t);
		data_control.saveData();
	}
	public abstract void updateStatus(long t);
	public abstract String getName();
	public abstract FacilityType getType();
	public abstract String imageLoc();
	
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public int getEndurance(){return endurance;}
	public void setEndurance(int e){endurance=e;}
	public OwnableSatellite<?> getLocation(){return location;}
	public void setLocation(OwnableSatellite<?> s){location=s;}
	
	public int getId(){return id;}
	public void setId(int i){id=i;}
}