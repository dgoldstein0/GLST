import java.util.Hashtable;
import javax.swing.SwingUtilities;

public abstract class OwnableSatellite extends Satellite
{
	Object facilities_lock = new Object();
	Hashtable<Integer, Facility> facilities;
	Base the_base; //the base is a member of facilities.  As such, it should be governed by facilities_lock.
	Player owner;
	
	//for building facilities
	int bldg_in_progress; //uses the constants specified in Facility
	long time_finish;
	long time_start;
	int next_facility_id;
	
	//for population model
	long population; //current population
	double initial_pop;
	double pop_capacity;
	double pop_growth_rate;
	
	//for taxation
	long last_tax_time=0;
	final static int TAX_INTERVAL = 3000; //in milliseconds.  so taxes are compounded every three seconds.
	final static double TAX_PER_PERSON = .03; //money per person per tax interval
	
	public OwnableSatellite()
	{
		next_facility_id=0;
		facilities = new Hashtable<Integer, Facility>();
	}
	
	//this is called when calculating taxes.
	private void updatePop(long t)
	{
		//this computes population based on a logarithmic model
		population = (long)(initial_pop*pop_capacity/(initial_pop+(pop_capacity-initial_pop)*Math.exp(-pop_growth_rate*((double)t))));
	}
	
	public double updatePopAndTax(long t)
	{
		//taxes are computed incrementally.
		double new_taxes=0;
		
		while(t-last_tax_time >= TAX_INTERVAL)
		{
			last_tax_time += TAX_INTERVAL;
			updatePop(last_tax_time);
			new_taxes += TAX_PER_PERSON*((double)population);
		}
		
		return new_taxes;
	}
	
	public void updateConstruction(GameInterface GI, long t)
	{
		if(bldg_in_progress != Facility.NO_BLDG)
		{
			if(t >= time_finish) //if build is finished...
			{
				Facility new_fac;
				switch(bldg_in_progress)
				{
					case Facility.BASE:
						new_fac = new Base(this, time_finish);
						synchronized(facilities_lock){
							the_base = (Base)new_fac;
						}
						break;
					case Facility.MINE:
						new_fac = new Mine(this, time_finish);
						break;
					case Facility.SHIPYARD:
						new_fac = new Shipyard(this, time_finish);
						break;
					default:
						System.out.println("updateConstruction does not support this facility type!  ending construction and returning.");
						bldg_in_progress = Facility.NO_BLDG;
						return;
				}
				SwingUtilities.invokeLater(new FacilityAdder(new_fac, GI));
				
				bldg_in_progress = Facility.NO_BLDG;
			}
		}
	}
	
	public class FacilityAdder implements Runnable
	{
		Facility new_fac;
		GameInterface GI;
		
		public FacilityAdder(Facility f, GameInterface gi)
		{
			new_fac = f;
			GI=gi;
		}
		
		public void run() //this must run on the swing thread because we want to avoid running PlanetMoonCommandPanel.setSat() and this at the same time, because that can result in the same facility being displayed twice
		{
			synchronized(facilities_lock)
			{
				facilities.put(new_fac.id,new_fac);
				
				//notify interface
				if(GI.sat_or_ship_disp == GameInterface.SAT_PANEL_DISP && GI.SatellitePanel.the_sat == new_fac.location)
				{
					GI.SatellitePanel.displayFacility(new_fac);
				}
			}
		}
	}
	
	//gives the percent of the build progress,  The result of this function is meaningless if bldg_in_progress==NO_BLDG
	public double constructionProgress(long t)
	{
		return ((double)(t-time_start))/((double)(time_finish-time_start));
	}
	
	//return value is true if the building will be built, and false if the player does not have enough money/metal to build it
	public boolean scheduleConstruction(int bldg_type, long start_time)
	{
		bldg_in_progress = bldg_type;
		time_start = start_time;
		
		long build_time;
		int met, mon; //metal and money costs
		//this switch deteremines the cost of the building.
		switch(bldg_type)
		{
			case Facility.BASE:
				met=GalacticStrategyConstants.BASE_METAL_COST;
				mon=GalacticStrategyConstants.BASE_MONEY_COST;
				build_time = GalacticStrategyConstants.BASE_BUILD_TIME;
				break;
			case Facility.MINE:
				met = GalacticStrategyConstants.MINE_METAL_COST;
				mon = GalacticStrategyConstants.MINE_MONEY_COST;
				build_time = GalacticStrategyConstants.MINE_BUILD_TIME;
				break;
			case Facility.SHIPYARD:
				met=GalacticStrategyConstants.SHIPYARD_METAL_COST;
				mon=GalacticStrategyConstants.SHIPYARD_MONEY_COST;
				build_time = GalacticStrategyConstants.SHIPYARD_BUILD_TIME;
				break;
			default:
				System.out.println("facility not recognized by schedule construction.  terminating construction.");
				cancelConstruction();
				return false;
		}
		time_finish = start_time+build_time;
		synchronized(owner.metal_lock){
			synchronized(owner.money_lock){
				if(owner.metal >= met && owner.money >= mon)
				{
					owner.metal -= met;
					owner.money -= mon; 
					//notify all players ***
					
					return true;
				}
				else
				{
					cancelConstruction();
					return false;
				}
			}
		}
	}
	
	public void cancelConstruction()
	{
		bldg_in_progress=Facility.NO_BLDG;
	}
	
	public void setOwner(Player p, long time)
	{
		setOwner(p);
		synchronized(facilities_lock)
		{
			for(Integer i: facilities.keySet())
			{
				facilities.get(i).last_time = time; //makes it so that facilities do nothing when possessed by noone.  That is, they lie idle instead of stockpiling production.
			}
		}
	}
	
	public Hashtable<Integer, Facility> getFacilities(){return facilities;}
	public void setFacilities(Hashtable<Integer, Facility> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public abstract void setOwner(Player p); //this must be overriden by implementing classes, because it is responsible for notifying the GSystem of an owner change
	public long getPopulation(){return population;}
	public void setPopulation(long pop){population=pop;}
	public double getInitial_pop(){return initial_pop;}
	public void setInitial_pop(double p){initial_pop=p;}
	public double getPop_capacity(){return pop_capacity;}
	public void setPop_capacity(double p){pop_capacity=p;}
	public double getPop_growth_rate(){return pop_growth_rate;}
	public void setPop_growth_rate(double r){pop_growth_rate = r;}
	public Base getThe_base(){return the_base;}
	public void setThe_base(Base b){the_base = b;}
	public int getBldg_in_progress(){return bldg_in_progress;}
	public void setBldg_in_progress(int b){bldg_in_progress=b;}
	
	public int getNext_facility_id(){return next_facility_id;}
	public void setNext_facility_id(int n){next_facility_id=n;}
}