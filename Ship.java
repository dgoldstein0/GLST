import java.util.HashSet;

public class Ship extends Targetter implements Targetable
{
	Player owner;
	
	ShipType type;
	String name;
	
	int energy;
	int max_energy;
	
	int hull_strength;
	int damage;
	
	GSystem location;
	double pos_x; //pos_x and pos_y indicate where in the system the ship is located
	double pos_y;
	
	double vel_x;
	double vel_y;
	
	int soldier;
	
	HashSet<Targetter> aggressors;
	
	public Ship(String nm, ShipType t)
	{
		name = nm;
		type = t;
		energy = t.max_energy;
		max_energy = energy;
		hull_strength = t.hull;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		damage=0;
		aggressors = new HashSet<Targetter>();
	}
	
	public void assemble(Shipyard builder, long t)
	{
		//the time dependence of this function needs to be established
		owner=builder.location.owner;
		pos_x = builder.default_x + builder.location.absoluteCurX();
		pos_y = builder.default_y + builder.location.absoluteCurY();
		vel_x=builder.location.orbit.getAbsVelX();
		vel_y=builder.location.orbit.getAbsVelY();
		location.fleets[owner.getId()].add(this);
		orderToMove(t, builder.assemble_x, builder.assemble_y); //this call does not go via the game Event handling system.  all computers should issue these orders on their own.
	}
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=hull_strength)
			destroyed();
	}
	
	public void orderToMove(long t, double x, double y)
	{
		
	}
	
	public void destroyed()
	{
		location.fleets[owner.getId()].remove(this);
	}
	
	//ship physics
	
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	
	//methods required for save/load
	public Ship(){}
	public ShipType getType(){return type;}
	public void setType(ShipType tp){type=tp;}
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
	public double getPos_x(){return pos_x;}
	public double getPos_y(){return pos_y;}
	public void setPos_x(double x){pos_x=x;}
	public void setPos_y(double y){pos_y=y;}
	public Player getOwner() {return owner;}
	public int getSoldier() {return soldier;}
}