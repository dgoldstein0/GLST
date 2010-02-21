import java.util.HashSet;

public class Ship extends Targetter implements Targetable
{
	Player owner;
	
	int type;
	String name;
	
	int energy;
	int max_energy;
	
	int hull_strength;
	int damage;
	
	GSystem location;
	int pos_x; //pos_x and pos_y indicate where in the system the ship is located
	int pos_y;
	
	int soldier;
	
	HashSet<Targetter> aggressors;
	
	public Ship(String nm, int type)
	{
		name = nm;
		this.type = type;
		energy = GalacticStrategyConstants.sTypes[type].max_energy;
		max_energy = energy;
		hull_strength = GalacticStrategyConstants.sTypes[type].hull;
		soldier=GalacticStrategyConstants.sTypes[type].soldier_capacity;//assume ships are fully loaded when built
		damage=0;
		aggressors = new HashSet<Targetter>();
	}
	
	public void assemble(Shipyard builder)
	{
		owner=builder.location.owner;
		pos_x = builder.assemble_x;
		pos_y = builder.assemble_y;
		location.fleets[owner.getId()].add(this);
	}
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=hull_strength)
			destroyed();
	}
	
	public void destroyed()
	{
		location.fleets[owner.getId()].remove(this);
	}
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	
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
	public Player getOwner() {return owner;}
	public int getSoldier() {return soldier;}
}