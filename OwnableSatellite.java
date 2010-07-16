import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.SwingUtilities;

public abstract class OwnableSatellite<T extends OwnableSatellite<T>> extends Satellite<T> implements RelaxedSaveable<T>, Orbitable<T>
{
	ArrayList<Satellite<?>> orbiting;
	
	Object facilities_lock = new Object();
	Hashtable<Integer, Facility<?> > facilities;
	Base the_base; //the base is a member of facilities.  As such, it should be governed by facilities_lock.
	Player owner;
	
	long time; //last time there was a change
	
	//for building facilities
	FacilityType bldg_in_progress; //uses the constants specified in Facility
	long time_finish;
	long time_start;
	int next_facility_id; //serves as id of facility in progress, as well as the next id counter
	
	//for population model
	long population; //current population
	double initial_pop;
	double pop_capacity;
	double pop_growth_rate;
	
	//for taxation
	long last_tax_time=0;
	long tax_money;
	final static int TAX_INTERVAL = 3000; //in milliseconds.  so taxes are compounded every three seconds.
	final static double TAX_PER_PERSON = .03; //money per person per tax interval
	
	OwnableSatelliteDataSaverControl<T> data_control;
	
	@Override
	public OwnableSatelliteDataSaverControl<T> getDataControl(){return data_control;}
	
	public OwnableSatellite()
	{
		next_facility_id=0;
		facilities = new Hashtable<Integer, Facility<?>>();
		bldg_in_progress = FacilityType.NO_BLDG;
		data_control = new OwnableSatelliteDataSaverControl<T>((T)this);
	}
	
	public void handleDataNotSaved(long t){System.out.println("OwnableSatellite data not saved.  Ridiculous!");}
	
	public void update(long time_elapsed)
	{
		time=time_elapsed;
		synchronized(facilities_lock)
		{
			for(Integer i: facilities.keySet())
				facilities.get(i).updateStatus(time_elapsed);
		}
		updateConstruction(time_elapsed);
		
		if(owner != null)
			owner.changeMoney(updatePopAndTax(time_elapsed));
		else
			updatePop(time_elapsed);
	}
	
	//this is called when calculating taxes.
	private void updatePop(long t)
	{
		//this computes population based on a logarithmic model
		population = (long)(initial_pop*pop_capacity/(initial_pop+(pop_capacity-initial_pop)*Math.exp(-pop_growth_rate*((double)t))));
	}
	
	private long updatePopAndTax(long t)
	{
		//taxes are computed incrementally to prevent rounding inconsistencies
		tax_money = 0;
		
		if(t-last_tax_time >= TAX_INTERVAL) //this will be called every every time grain, since it is now within the loop in UpdateGame, so to have it be a while instead of an if is misleading.
		{
			last_tax_time += TAX_INTERVAL;
			updatePop(last_tax_time);
			tax_money = (long) (TAX_PER_PERSON*((double)population));
			data_control.saveData();
		}
		
		return tax_money;
	}
	
	private void updateConstruction(long t)
	{
		if(bldg_in_progress != FacilityType.NO_BLDG)
		{
			if(t >= time_finish) //if build is finished...
			{
				Facility<?> new_fac = bldg_in_progress.creator.create(this, next_facility_id++, time_finish);
				if(bldg_in_progress == FacilityType.BASE)
				{
					synchronized(facilities_lock){
						the_base = (Base)new_fac;
					}
				}

				synchronized(facilities_lock)
				{
					facilities.put(new_fac.id,new_fac);
				}
				SwingUtilities.invokeLater(new FacilityAdder(new_fac, GameInterface.GC.GI));
				
				bldg_in_progress = FacilityType.NO_BLDG;
				data_control.saveData();
			}
		}
	}
	
	public class FacilityAdder implements Runnable
	{
		Facility<?> new_fac;
		GameInterface GI;
		
		public FacilityAdder(Facility<?> f, GameInterface gi)
		{
			new_fac = f;
			GI=gi;
		}
		
		public void run() //this must run on the swing thread because we want to avoid running PlanetMoonCommandPanel.setSat() and this at the same time, because that can result in the same facility being displayed twice
		{
			synchronized(facilities_lock)
			{
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
	public boolean scheduleConstruction(FacilityType bldg_type, long start_time)
	{
		bldg_in_progress = bldg_type;
		time_start = start_time;
		
		long build_time = bldg_type.build_time;
		int met = bldg_type.metal_cost, mon=bldg_type.money_cost; //metal and money costs
		
		time_finish = start_time+build_time;
		
		synchronized(owner.metal_lock){
			synchronized(owner.money_lock){
				if(owner.metal >= met && owner.money >= mon)
				{
					owner.metal -= met;
					owner.money -= mon; 
					
					data_control.saveData();
					//notify all players ***
					
					return true;
				}
				else
				{
					bldg_in_progress=FacilityType.NO_BLDG;
					return false;
				}
			}
		}
	}
	
	public void cancelConstruction()
	{
		bldg_in_progress=FacilityType.NO_BLDG;
		data_control.saveData();
	}
	
	public void setOwner(Player p, long time)
	{
		if(owner != null)
			update(time);
		else
		{
			this.time=time;
			synchronized(facilities_lock)
			{
				for(Integer i: facilities.keySet())
				{
					facilities.get(i).last_time = time; //makes it so that facilities do nothing when possessed by no one.
											//That is, they lie idle instead of stockpiling production.
					facilities.get(i).data_control.saveData();
				}
			}
		}
		
		//start counting taxes from here
		last_tax_time = time;
		
		setOwner(p);
		data_control.saveData();
	}
	
	public Hashtable<Integer, Facility<?>> getFacilities(){return facilities;}
	public void setFacilities(Hashtable<Integer, Facility<?>> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public abstract void setOwner(Player p); //this must be overridden by implementing classes, because it is responsible for notifying the GSystem of an owner change
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
	public FacilityType getBldg_in_progress(){return bldg_in_progress;}
	public void setBldg_in_progress(FacilityType b){bldg_in_progress=b;}
	
	public int getNext_facility_id(){return next_facility_id;}
	public void setNext_facility_id(int n){next_facility_id=n;}
	
	public ArrayList<Satellite<?>> getOrbiting(){return orbiting;}
	public void setOrbiting(ArrayList<Satellite<?>> o){orbiting=o;}
	
	public long getTime(){return time;}
	public void setTime(long t){time=t;}
}