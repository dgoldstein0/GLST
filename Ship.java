public class Ship
{
	Player owner;
	
	int type;
	String name;
	
	int energy;
	int max_energy;
	
	int hull_strength;
	int damage;
	
	int pos_x;
	int pos_y;
	
	int cost;
	int soldier;
	
	Ship target;
	
	public Ship(Player ow, String nm, int type)
	{
		name = nm;
		this.type = type;
		owner=ow;
		energy = GalacticStrategyConstants.sTypes[type].max_energy;
		max_energy = GalacticStrategyConstants.sTypes[type].max_energy;
		hull_strength = GalacticStrategyConstants.sTypes[type].hull;
		cost=GalacticStrategyConstants.sTypes[type].cost;
		soldier=GalacticStrategyConstants.sTypes[type].soldier_capacity;						//assume ships are fully loaded when built
		damage=0;
	}
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=hull_strength)
			destroyed();
	}
	
	public void destroyed()
	{
	}
	
	//methods required for save/load
	public Ship(){}
	public int getType(){return type;}
	public void setType(int tp){type=tp;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public int getEnergy(){return energy;}
	public void setEnergy(int f){energy=f;}
	public int getMax_energy(){return max_energy;}
	public void setMax_energy(int mf){max_energy=mf;}
	public int getHull_strength(){return hull_strength;}
	public void setHull_strength(int hs){hull_strength=hs;}
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public void setX(int x){pos_x=x;}
	public void setY(int y){pos_y=y;}
	public void assemble(int x, int y) {};
	public Player getOwner() {return owner;}
	public int getSoldier() {return soldier;}
	public Ship getTarget(){return target;}
	public void setTarget(Ship s){target = s;}
}