public class Ship
{
	static int JUNK=0;
	static ShipType[] sTypes={new ShipType("Junk",20,100)};
	
	int type;
	String name;
	
	int energy;
	int max_energy;
	
	int hull_strength;
	int damage;
	
	public Ship(String nm, int type)
	{
		name = nm;
		this.type = type;
		energy = sTypes[type].max_energy;
		max_energy = sTypes[type].max_energy;
		hull_strength = sTypes[type].hull;
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
}