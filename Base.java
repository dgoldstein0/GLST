
public class Base extends Facility{
	
	float soldier;
	double last_time;
	
	public Base(double t)
	{
		damage=0;
		last_time = t;
		soldier=GalacticStrategyConstants.initial_soldier;
		endurance=GalacticStrategyConstants.initial_base_endu;
	}
	
	public void upgrade()
	{
		soldier+= GalacticStrategyConstants.solider_upgraderate;
		endurance+= GalacticStrategyConstants.endu_upgraderate;
	}
	
	public void taken(Player enemy)
	{
		location.setOwner(enemy);
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
