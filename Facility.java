
import java.util.HashSet;

public abstract class Facility implements Targetable, RelaxedSaveable
{	
	OwnableSatellite location;
	int id;
	
	HashSet<Targetter> aggressors;
	int endurance;
	int damage;
	
	long last_time;//the last time it was updated
	
	RelaxedDataSaverControl<? extends Facility> data_control; //must be instantiated by subclasses
	
	public Facility(OwnableSatellite l, long t, int endu)
	{
		location=l;
		
		id=l.next_facility_id;
		l.next_facility_id++;
		
		last_time=t;
		
		endurance=endu;
		damage=0;
		aggressors = new HashSet<Targetter>();
	}
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=endurance)
			destroyed();
		data_control.saveData();
	}
	
	public void destroyed() //default option.  Base overrides this
	{
		location.facilities.remove(this);
	}
	
	public DestDescriber describer()
	{
		return new FacilityDescriber(this);
	}
	
	public void handleDataNotSaved(){removeFromGame();}
	
	public void removeFromGame()
	{
		//should probably cache the facility for recall... but anyway
		location.facilities.remove(this);
	}
	
	public Facility(){}
	
	public long getLast_time(){return last_time;}
	public void setLast_time(long t){last_time=t;}
	public double getXCoord(long t){return location.getXCoord(t);}
	public double getYCoord(long t){return location.getYCoord(t);}
	public double getXVel(long t){return location.getXVel(t);}
	public double getYVel(long t){return location.getYVel(t);}
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	public void addAggressor(Targetter t){aggressors.add(t);}
	public void removeAggressor(Targetter t){aggressors.remove(t);}
	public abstract void updateStatus(long t);
	public abstract String getName();
	public abstract FacilityType getType();
	public abstract String imageLoc();
	
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public int getEndurance(){return endurance;}
	public void setEndurance(int e){endurance=e;}
	public OwnableSatellite getLocation(){return location;}
	public void setLocation(OwnableSatellite s){location=s;}
	
	public int getId(){return id;}
	public void setId(int i){id=i;}
}