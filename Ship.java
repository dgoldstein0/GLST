import java.util.HashSet;

public class Ship extends Flyer implements Selectable
{
	Player owner;
	int id;
	
	int energy;
	int max_energy;
	
	float soldier;
	
	public Ship(String nm, ShipType t, int id)
	{
		this.id=id;
		name = nm;
		type = t;
		energy = t.max_energy;
		max_energy = energy;
		hull_strength = t.hull;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		damage=0;
		aggressors = new HashSet<Targetter>();
		ship_data=new ShipDataSaver[data_capacity];
		for(int i=0; i<ship_data.length; i++)
			ship_data[i] = new ShipDataSaver();
		index=0;
		
		current_flying_AI = new TrackingAI(this);
	}
	
	public void assemble(Shipyard builder, long t)
	{
		//the time dependence of this function needs to be established
		owner=builder.location.owner;
		
		//set the position of the planet/moon correctly.  we do not need to restore the position, because in the updateGame function the orbit.move command is given after all facilities are updated
		builder.location.orbit.move(t);
		if(builder.location instanceof Moon)
			((Planet)builder.location.orbit.boss).orbit.move(t);
		pos_x = builder.default_x + builder.location.absoluteCurX();
		pos_y = builder.default_y + builder.location.absoluteCurY();
		double vel_x=builder.location.orbit.getAbsVelX();
		double vel_y=builder.location.orbit.getAbsVelY();
		direction = Math.atan2(vel_y, vel_x);
		speed = Math.hypot(vel_x, vel_y);
		if(builder.location instanceof Planet)
			location = (GSystem)builder.location.orbit.boss;
		else //builder.location is a Moon
			location = (GSystem) ((Planet)builder.location.orbit.boss).orbit.boss;
		location.fleets[owner.getId()].add(this);
		orderToMove(t, builder.location); //this call does not go via the game Event handling system.  all computers should issue these orders on their own.
	}
	
	public void orderToMove(long t, Destination d)
	{
		destination = d;
		time = (long)(Math.ceil((double)(t)/(double)(time_granularity))*time_granularity);
		dest_x_coord = d.getXCoord(time-time_granularity);
		dest_y_coord = d.getYCoord(time-time_granularity);
	}
	
	public void destroyed()
	{
		location.fleets[owner.getId()].remove(this);
	}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	//methods required for save/load
	public Ship(){}
	public int getEnergy(){return energy;}
	public void setEnergy(int f){energy=f;}
	public int getMax_energy(){return max_energy;}
	public void setMax_energy(int mf){max_energy=mf;}
	public int getHull_strength(){return hull_strength;}
	public void setHull_strength(int hs){hull_strength=hs;}
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public Player getOwner() {return owner;}
	public float getSoldier() {return soldier;}
	public void setId(int i){id=i;}
	public int getId(){return id;}
}