import java.util.Random;

public strictfp class Base extends Facility<Base>{
	
	final Object soldier_lock = new Object();
	float soldier;
	int max_soldier;
	
	public Base(OwnableSatellite<?> l, int i, long t)
	{
		super(l, i, GalacticStrategyConstants.initial_base_endu);
		soldier = GalacticStrategyConstants.initial_soldier;
		max_soldier = GalacticStrategyConstants.default_max_soldier;
	}
	
	public void upgrade()
	{
		max_soldier+= GalacticStrategyConstants.max_soldier_upgraderate;
		endurance+= GalacticStrategyConstants.endu_upgraderate;
	}
	
	public void updateStatus(long time)
	{
		synchronized(soldier_lock)
		{
			soldier += GalacticStrategyConstants.soldier_production_rate*GalacticStrategyConstants.TIME_GRANULARITY;
			if(soldier > max_soldier)
				soldier=(float)max_soldier;
		}
	}
	
	public float retrieveSoldiers(float asking)
	{
		float giving; //the number of soldiers the Base is giving up to the ship
		synchronized(soldier_lock)
		{
			if(soldier > asking)
			{
				soldier -= asking;
				giving = asking;
			}
			else
			{
				giving = soldier;
				soldier = 0.0f;
			}
		}
		
		return giving;
	}
	
	public void attackedByTroops(long t, Ship enemy) 
	{
		synchronized(soldier_lock)
		{
			Random gen = new Random(t);
			
			while(soldier >= 1 && enemy.getSoldier() >= 1)
			{
				double probability=(double) (enemy.getSoldier())/(getSoldier()+enemy.getSoldier());
				double p=gen.nextDouble();
				if (p<probability)
					soldier--;
				else
					enemy.soldier--;
			}
			
			//figure the results
			if(soldier < 1)
			{
				location.setOwnerAtTime(enemy.getOwner(), t);
				
				//TODO: notify player
			}
		}
	}
	
	@Override
	public void ownerChanged(long t)
	{
		soldier=0;
	}
	
	public void destroyed()
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
			location.setOwner(null);
			location.the_base = null;
		}
	}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	public Base(){}
	
	public float getSoldier(){return soldier;}
	public void setSoldier(float s){synchronized(soldier_lock){soldier=s;}}
	public int getMax_soldier(){return max_soldier;}
	public void setMax_soldier(int s){max_soldier=s;}
	
	public FacilityType getType(){return FacilityType.BASE;}
}
