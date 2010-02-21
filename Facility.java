import java.util.HashSet;

public abstract class Facility implements Targetable
{
	private static int cost_to_build;
	private static int time_to_build;
	Planet location;
	HashSet<Targetter> aggressors;
	int endurance;
	int damage;
	
	public static int getCost_to_build(){return cost_to_build;}
	public static int getTime_to_build(){return time_to_build;}
	
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
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	public void updateStatus(long t){}
}