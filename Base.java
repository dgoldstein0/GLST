
public class Base extends Facility{
	
	float soldier;
	int max_soldier;
	double last_time;
	
	public Base(double t)
	{
		damage=0;
		last_time = t;
		soldier=GalacticStrategyConstants.initial_soldier;
		endurance=GalacticStrategyConstants.initial_base_endu;
		max_soldier = GalacticStrategyConstants.default_max_soldier;
	}
	
	public void upgrade()
	{
		max_soldier+= GalacticStrategyConstants.max_soldier_upgraderate;
		endurance+= GalacticStrategyConstants.endu_upgraderate;
	}
	
	public void taken(Player enemy)
	{
		location.setOwner(enemy);
	}
	
	public void updateStatus(long time)
	{
		soldier += GalacticStrategyConstants.soldier_production_rate*(time-last_time);
		if(soldier > max_soldier)
			soldier=(float)max_soldier;
		last_time=time;
	}
	
	public void attackedByTroops(Ship enemy) 
	{
		double probability=(double) (enemy.getSoldier())/(getSoldier()+enemy.getSoldier());
		double p=Math.random();
		if (p<probability)
			taken(enemy.getOwner());
	}
	
	public void destroyed()
	{
		location.facilities.remove(this);
		location.setOwner(null);
	}
	
	public int getSoldier(){return (int)Math.floor(soldier);}
}
