import java.util.ArrayList;

public abstract class OwnableSatellite extends Satellite
{
	ArrayList<Facility> facilities;
	Player owner;
	
	//for building facilities
	int bldg_in_progress; //uses the constants specified in Facility
	long time_finish;
	long time_start;
	
	//for population model
	long population; //current population
	double initial_pop;
	double pop_capacity;
	double pop_growth_rate;
	
	//for taxation
	long last_tax_time=0;
	final static int TAX_INTERVAL = 3000; //in milliseconds.  so taxes are compounded every three seconds.
	final static double TAX_PER_PERSON = .03; //money per person per tax interval
	
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
						new_fac = new Base(time_finish);
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
				facilities.add(new_fac);
				
				//notify interface
				if(GI.SatellitePanel.the_sat == this)
					GI.SatellitePanel.displayFacility(new_fac);
				
				bldg_in_progress = Facility.NO_BLDG;
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
	
	public ArrayList<Facility> getFacilities(){return facilities;}
	public void setFacilities(ArrayList<Facility> fac){facilities=fac;}
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
}