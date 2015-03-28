package galactic_strategy.game_objects;

import galactic_strategy.Constants;
import galactic_strategy.Player;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Saveable;
import galactic_strategy.sync_engine.TimeControl;
import galactic_strategy.ui.DestDisplayUpdater;
import galactic_strategy.ui.GameInterface;
import galactic_strategy.ui.ObjBuilder;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

public strictfp abstract class OwnableSatellite<T extends OwnableSatellite<T>> extends Satellite<T> implements Saveable<T>, Orbitable<T>
{
	List<Satellite<?>> orbiting;
	
	/**@GuardedBy facilities*/
	HashMap<Integer, Facility<?> > facilities;
	
	/**@GuardedBy facilities*/
	Base the_base; //the base is a member of facilities.  As such, it should be governed by facilities_lock.
	
	Player owner;
	Color lastcolor;
	
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
	int building_limit;
	
	public int number_mines; //cache number of mines on satellite
	int last_number_mines;
	public int number_taxoffices; //cache number of taxoffices on satellite
	double base_mining_r;
	double current_mining_r;
	
	
	//for taxation
	//long last_tax_time;
	//long tax_money;
	//final static int TAX_INTERVAL = 3000; //in milliseconds.  so taxes are compounded every three seconds.
	//final static double TAX_PER_PERSON = .03; //money per person per tax interval
	
	DataSaverControl<T> data_control;
	
	@Override
	public DataSaverControl<T> getDataControl(){return data_control;}
	
	public OwnableSatellite()
	{
		init();
	}
	
	private void init() {
		next_facility_id=0;
		facilities = new HashMap<Integer, Facility<?>>();
		bldg_in_progress = FacilityType.NO_BLDG;
		
		@SuppressWarnings("unchecked")
		T typed_this = (T) this;
		
		data_control = new DataSaverControl<T>(typed_this);
		//last_tax_time = 0;
		owner = null;
		lastcolor=Color.WHITE;
		
		number_mines=0;
		last_number_mines=0;
		number_taxoffices=0;
	}
	
	public void update(long time_elapsed)
	{
		time=time_elapsed;
		synchronized(facilities)
		{
			for(Integer i: facilities.keySet())
				facilities.get(i).updateStatus(time_elapsed);
		}
		updateConstruction(time_elapsed);
		updatePop(time_elapsed);
	}
	
	public double calcMiningrate(){
		if(last_number_mines!=number_mines){
			last_number_mines=number_mines;
			current_mining_r = base_mining_r;
			for(int j=1;j<number_mines;j++){
				current_mining_r*=Constants.additional_mine_penalty;
			}
		}
		return current_mining_r;
	}
	
	
	//this is called when calculating taxes.
	private void updatePop(long t)
	{
		//this computes population based on a logarithmic model
		population = (long)(initial_pop*pop_capacity/(initial_pop+(pop_capacity-initial_pop)*Math.exp(-pop_growth_rate*((double)t))));
	}
	
	private void updateConstruction(long t)
	{
		if (bldg_in_progress != FacilityType.NO_BLDG)
		{
			if(t >= time_finish) //if build is finished...
			{
				Facility<?> new_fac = bldg_in_progress.creator.create(this, next_facility_id++, time_finish);
				synchronized(facilities){
					if(bldg_in_progress == FacilityType.BASE)
					{
						the_base = (Base)new_fac;
					}
					
					facilities.put(new_fac.id,new_fac);
				}
				SwingUtilities.invokeLater(new FacilityAdder(new_fac, GameInterface.GC.GI));
				bldg_in_progress = FacilityType.NO_BLDG;
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
			if(GI != null) //to satisfy testing framework
			{
				synchronized(facilities)
				{
					//notify interface
					if(GI.displayed_control_panel == GameInterface.PANEL_DISP.SAT_PANEL && GI.SatellitePanel.the_sat == new_fac.location)
					{
						GI.SatellitePanel.displayFacility(new_fac);
					}
				}
			}
		}
	}
	
	//gives the percent of the build progress,  The result of this function is meaningless if bldg_in_progress==NO_BLDG
	public double constructionProgress(long t)
	{
		return ((double)(t-time_start))/((double)(time_finish-time_start));
	}
	
	/**@return true if the building could be built right now, false otherwise.
	 * this will return the same value as scheduleConstruction, but is meant
	 * to be called by GUI code to check if an order makes sense before scheduling it.
	 * Calling this function doesn't guarantee that the metal/money will be there
	 * when you try to build instead of just checking if you can.
	 */
	public boolean canBuild(FacilityType bldg_type)
	{
		int met = bldg_type.metal_cost, mon=bldg_type.money_cost; //metal and money costs
		
		synchronized(facilities){
			synchronized(owner.getMetal_lock()){
				synchronized(owner.getMoney_lock()){
					if(owner.getMetal() >= met && owner.getMoney() >= mon && facilities.size() < building_limit)
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
	}
	
	/**@return true if the building will be built, false if not successful*/
	public boolean scheduleConstruction(FacilityType bldg_type, long start_time)
	{
		bldg_in_progress = bldg_type;
		time_start = TimeControl.roundUpToTimeGrain(start_time);
		
		long build_time = bldg_type.build_time;
		int met = bldg_type.metal_cost, mon=bldg_type.money_cost; //metal and money costs
		
		time_finish = TimeControl.roundUpToTimeGrain(start_time+build_time);
		
		synchronized(facilities){
			synchronized(owner.getMetal_lock()){
				synchronized(owner.getMoney_lock()){
					if(owner.getMetal() >= met && owner.getMoney() >= mon && facilities.size() < building_limit)
					{
						owner.changeMetal(-met);
						owner.changeMoney(-mon); 
						
						SwingUtilities.invokeLater(ObjBuilder.facilityManufactureFuncs.getCallback(this));
						
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
	}
	
	//Cannot do instantly - getting back resources early would be problematic
	public void cancelConstruction(long t)
	{
		double frac_remaining = (time_finish - t)/((double)bldg_in_progress.build_time);
		
		synchronized(owner.getMetal_lock())
		{
			synchronized(owner.getMoney_lock())
			{
				owner.changeMoney((long)(frac_remaining*bldg_in_progress.money_cost));
				owner.changeMetal((long)(frac_remaining*bldg_in_progress.metal_cost));
			}
		}
		
		bldg_in_progress=FacilityType.NO_BLDG;
	}
	
	
	public void changeOwnerAtTime(Player p, long time)
	{
		if(owner != null)
			update(time);
		else
		{
			this.time=time;
			synchronized(facilities)
			{
				for(Integer i: facilities.keySet())
				{
					facilities.get(i).ownerChanged(time); /*lets facilities change their states to adjust
					 								to new ownership.  In most implementations, this means
					 								that facilities will lie idle when unowned, instead of
					 								possibly stock-piling resources for the next invader.*/
				}
			}
		}
		
		//start counting taxes from here
		//last_tax_time = time;
		
		changeOwner(p);
		
		SwingUtilities.invokeLater(new DestDisplayUpdater(this));
	}
	
	public abstract GSystem getGSystem();
	public void changeOwner(Player p)
	{
		if(owner != null)
			getGSystem().decreaseClaim(owner);
		owner=p;
		if (p != null)
			getGSystem().increaseClaim(p);
	}
	
	
	public HashMap<Integer, Facility<?>> getFacilities(){return facilities;}
	public void setFacilities(HashMap<Integer, Facility<?>> fac){facilities=fac;}
	public Player getOwner(){return owner;}
	public void setOwner(Player p){owner = p;}
	
	public void setlastcolor(Color color){lastcolor=color;}
	public Color getlastcolor(){return lastcolor;}
	public double getBase_mining_rate(){return base_mining_r;}
	public void setBase_mining_rate(double r){base_mining_r = r;}
	public int getBuilding_limit(){return building_limit;}
	public void setBuilding_limit(int b){building_limit=b;}
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
	
	public List<Satellite<?>> getOrbiting(){return orbiting;}
	public void setOrbiting(List<Satellite<?>> o){orbiting=o;}
	
	public long getTime(){return time;}
	public void setTime(long t){time=t;}

	public int compareTo(OwnableSatellite<?> sat) {
		
		if (sat == null)
			return 1;
		
		if (orbit == null)
		{
			if (sat.orbit != null)
				return -1;
		}
		else
		{
			if (sat.orbit == null)
				return 1;
			
			if (orbit.boss != null || sat.orbit.boss != null)
			{
				if (orbit.boss == null)
					return -1;
				else if (sat.orbit.boss == null)
					return 1;
				
				//none of orbit, orbit.boss, sat.orbit, and sat.orbit.boss are null.
				
				int boss_compare = orbit.boss.compareTo(sat.orbit.boss);
				
				if(boss_compare != 0)
					return boss_compare;
			}
		}
		
		if (id < sat.id)
			return -1;
		else if (id == sat.id)
			return 0;
		else
			return 1;
	}
	
	@Override
	public int compareTo(Orbitable<?> o) {
		if (o instanceof OwnableSatellite<?>)
		{
			return compareTo((OwnableSatellite<?>) o);
		}
		else
			return -1;
	}
	
	@Override
	public void recursiveSaveData(long time) {
		data_control.saveData(time);
		orbit.data_control.saveData(time);
		
		for(Integer id : facilities.keySet())
		{
			facilities.get(id).data_control.saveData(time);
		}
		
		if (orbiting != null)
		{
			for(Satellite<?> sat : orbiting)
			{
				sat.recursiveSaveData(time);
			}
		}
	}

	@Override
	public void recursiveRevert(long t) throws DataSaverControl.DataNotYetSavedException {
		data_control.revertToTime(t);
		orbit.data_control.revertToTime(t);
		for(Integer id : facilities.keySet())
		{
			facilities.get(id).data_control.revertToTime(t);
		}
		
		if (orbiting != null)
		{
			for(Satellite<?> sat : orbiting)
			{
				sat.recursiveRevert(t);
			}
		}
	}

	public void setAsStartLocation(Player player) {
		owner = player;
		Base b = new Base(this, next_facility_id++, (long)0);
		facilities.put(b.id,b);
		the_base = b;
		getGSystem().increaseClaim(player);
	}
}