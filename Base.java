
public class Base extends Facility{
	
	int soldier;
	int damage;
	int endurance;	

	public Base()
	{
		damage=0;
		soldier=GalacticStrategyConstants.initial_soldier;
		endurance=GalacticStrategyConstants.initial_endu;
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
	
	
	public void attackedByCombat(Ship enemy) 
	{
		double probability=(double) (enemy.getSoldier())/(soldier+enemy.getSoldier());
		double p=Math.random();
		if (p<probability)
			taken(enemy.getOwner());
	}
	
	public void attackedByBruteForce(Ship enemy)
	{
		
	}
		
}
