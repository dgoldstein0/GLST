
public class Base extends Facility{
	
	static int initial_soldier=1000;
	static int initial_endu=100;
	static int solider_upgraderate=1000;
	static int endu_upgraderate=100;
		
	int soldier;
	int damage;
	int endurance;	

	public Base()
	{
		damage=0;
		soldier=initial_soldier;
		endurance=initial_endu;
	}
	
	public void upgrade()
	{
		soldier+=solider_upgraderate;
		endurance+=endu_upgraderate;
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
