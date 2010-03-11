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
	
	double pos_x1; //pos_x and pos_y indicate where in the system the ship is located
	double pos_y1;
	double direction1;
	double speed1;
	long time1;
	
	double pos_x2;
	double pos_y2;	
	double direction2;
	double speed2;
	long time2;
	
	double max_speed = .04; //px per millisecond
	double max_angular_vel = .001;//radians per milli
	double closing_radius = 20;
	
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
		
		//set the position of the planet/moon correctly.  we do not need to restore the position, because in the updateGame function the orbit.move command is given after all facilities are updated
		builder.location.orbit.move(t);
		if(builder.location instanceof Moon)
			((Planet)builder.location.orbit.boss).orbit.move(t);
		pos_x2 = builder.default_x + builder.location.absoluteCurX();
		pos_y2 = builder.default_y + builder.location.absoluteCurY();
		double vel_x=builder.location.orbit.getAbsVelX();
		double vel_y=builder.location.orbit.getAbsVelY();
		direction2 = Math.atan2(vel_y, vel_x);
		speed2 = Math.hypot(vel_x, vel_y);
		time2=t;
		if(builder.location instanceof Planet)
			location = (GSystem)builder.location.orbit.boss;
		else //builder.location is a Moon
			location = (GSystem) ((Planet)builder.location.orbit.boss).orbit.boss;
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
	static long time_granularity = 5;
	
	public void move(long t)
	{
		while(time2 < t)
		{
			time1=time2;
			time2 += time_granularity;
			
			pos_x1 = pos_x2;
			pos_y1 = pos_y2;
			
			direction1 = direction2;
			speed1 = speed2;
			
			//change position
			
			pos_x2 += speed1*time_granularity*Math.cos(direction1);
			pos_y2 += speed1*time_granularity*Math.sin(direction1);
			
			//change speed
			speed2 = Math.min(max_speed, Math.hypot(destinationX()-pos_x2, destinationY()-pos_y2)/closing_radius*max_speed);
			
			//change direction
			double dest_vec_x = destinationX() - pos_x2;
			double dest_vec_y = destinationY() - pos_y2;
			double desired_change = Math.atan2(dest_vec_y, dest_vec_x)-direction2;
			if(desired_change > Math.PI)
				desired_change -= 2*Math.PI;
			else if(desired_change < -Math.PI)
				desired_change += 2*Math.PI;
			direction2 += Math.min(desired_change, Math.abs(desired_change)*max_angular_vel*time_granularity);
		}
	}
	
	private double destinationX()
	{
		return 800;
	}
	
	private double destinationY()
	{
		return 0;
	}
	
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
	public double getPos_x1(){return pos_x1;}
	public double getPos_y1(){return pos_y1;}
	public void setPos_x1(double x){pos_x1=x;}
	public void setPos_y1(double y){pos_y1=y;}
	public double getPos_x2(){return pos_x2;}
	public double getPos_y2(){return pos_y2;}
	public void setPos_x2(double x){pos_x2=x;}
	public void setPos_y2(double y){pos_y2=y;}
	public Player getOwner() {return owner;}
	public int getSoldier() {return soldier;}
}