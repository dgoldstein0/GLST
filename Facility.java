
import java.util.HashSet;

public abstract class Facility implements Targetable
{
	final static int NO_BLDG=0;
	final static int BASE = 1;
	final static int MINE=2;
	final static int SHIPYARD=3;
	final static int RESEARCH_BUILDING=4;
	
	OwnableSatellite location;
	HashSet<Targetter> aggressors;
	int endurance;
	int damage;
	
	long last_time;//the last time it was updated
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=endurance)
			destroyed();
	}
	
	public void destroyed() //default option.  Base overrides this
	{
		location.facilities.remove(this);
	}
	
	public double getXCoord(long t){return location.getXCoord(t);}
	public double getYCoord(long t){return location.getYCoord(t);}
	public double getXVel(long t){return location.getXVel(t);}
	public double getYVel(long t){return location.getYVel(t);}
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	public abstract void updateStatus(long t);
	public abstract String getName();
	public abstract int getType();
	public abstract String imageLoc();
	
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public int getEndurance(){return endurance;}
	public void setEndurance(int e){endurance=e;}
	public OwnableSatellite getLocation(){return location;}
	public void setLocation(OwnableSatellite s){location=s;}
}