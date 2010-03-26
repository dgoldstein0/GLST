import java.util.HashSet;

public class Ship extends Targetter implements Targetable, Selectable, Destination
{
	static final int data_capacity=50;
	
	Player owner;
	int id;
	
	ShipType type;
	String name;
	
	int energy;
	int max_energy;
	
	int hull_strength;
	int damage;
	
	GSystem location;
	
	Destination destination;
	//save in case SystemPainter wants to paint crosshairs
	double dest_x_coord;
	double dest_y_coord;
	
	double pos_x; //pos_x and pos_y indicate where in the system the ship is located
	double pos_y;
	double direction;
	double speed;
	long time;
	
	ShipDataSaver ship_data[];
	int index;  //for the data array
	
	double max_speed = .04; //px per millisecond
	double max_angular_vel = .0005;//radians per milli
	
	float soldier;
	
	HashSet<Targetter> aggressors;
	
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
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=hull_strength)
			destroyed();
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
	
	//ship physics
	static long time_granularity = 20;
	static double delta = .5d;
	
	public void move(long t)
	{
		while(time < t)
			moveIncrement();
	}
	
	//moves the ship one time_granularity.  this is a separate function so that all ships updates can be stepped through 1 by 1.
	public void moveIncrement()
	{
		//change position
		pos_x += speed*time_granularity*Math.cos(direction);
		pos_y += speed*time_granularity*Math.sin(direction);
		
		if(Math.abs(pos_x - destinationX()) < delta && Math.abs(pos_y - destinationY()) < delta)
		{
			pos_x = destinationX();
			pos_y = destinationY();
			speed = 0.0d;
		}
		else
		{
			//change direction
			double dest_vec_x = destinationX() - pos_x;
			double dest_vec_y = destinationY() - pos_y;
			double desired_change = Math.atan2(dest_vec_y, dest_vec_x)-direction;

			if(desired_change > Math.PI)
				desired_change -= 2*Math.PI;
			else if(desired_change < -Math.PI)
				desired_change += 2*Math.PI;
			
			//change speed
			if(desired_change < Math.PI/2.0 && desired_change > -Math.PI/2.0)
				speed = Math.min(max_speed*Math.cos(desired_change)*Math.cos(desired_change), Math.hypot(destinationX()-pos_x, destinationY()-pos_y) * Math.cos(desired_change)/GalacticStrategyConstants.LANDING_RANGE*max_speed);
			else
				speed = 0.0d;
			
			double actual_chng = Math.min(Math.abs(desired_change), Math.abs(max_angular_vel*time_granularity)); //finds the absolute value of the amount the direction changes
			if(desired_change > 0)
				direction += actual_chng;
			else
				direction -= actual_chng;
			
			if(direction > Math.PI)
				direction -= 2*Math.PI;
			else if(direction<-Math.PI)
				direction += 2*Math.PI;
		}
		time += time_granularity;
		
		//save data
		saveData();
	}
	
	public void saveData()
	{
		ship_data[index].dir=direction;
		ship_data[index].px=pos_x;
		ship_data[index].py=pos_y;
		ship_data[index].t=time;
		ship_data[index].sp=speed;
		index++;
		if (index>data_capacity-1)
			index=0;
	}
	
	public void loadData(long t)
	{
		saveindex = index;
		int stepback=(int) (Math.floor((time-t)/time_granularity) + 1);
		//System.out.println("load data: t is " + Long.toString(t) + " and time is " + Long.toString(time) + ", so step back... " + Integer.toString(stepback));
		if (stepback>50)
		{
			System.out.println("Error loading ship data: the delay is too long");
		}
		else
			for (int i=0; i<stepback; i++)
			{
				index--;
				if (index<0)
					index=data_capacity-1;
			}
		
		if (ship_data[index]!=null)
		{
			direction=ship_data[index].dir;
			pos_x=ship_data[index].px;
			pos_y=ship_data[index].py;
			time=ship_data[index].t;
			speed=ship_data[index].sp;
		}
		else System.out.println("Error loading ship data: data wasn't saved");	//CAN THIS HAPPEN?
	}
	
	private int saveindex;
	
	private void restoreIndex()
	{
		index=saveindex;
	}
	
	private double destinationX()
	{
		dest_x_coord = destination.getXCoord(time);
		return dest_x_coord;
	}
	
	private double destinationY()
	{
		dest_y_coord = destination.getYCoord(time);
		return dest_y_coord;
	}
	
	public HashSet<Targetter> getAggressors(){return aggressors;}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	//methods to implement destination
	public String imageLoc(){return type.getImg_loc();}
	
	long dest_coords_time=0; //there cannot be a call to getXCoord or getYCoord with t=0, since ships do not exist then, and if they do will not be targetting other ships
	double dest_pos_x;
	double dest_pos_y;
	
	public double getXCoord(long t)
	{
		if(t == dest_coords_time)
			return dest_pos_x;
		else
		{			
			updateDestData(t);
			return dest_pos_x;
		}
	}
	
	public double getYCoord(long t)
	{
		if(t == dest_coords_time)
			return dest_pos_y;
		else
		{
			updateDestData(t);
			return dest_pos_y;
		}
	}
	
	private void updateDestData(long t)
	{
		double temp_dir = direction;
		double temp_x = pos_x;
		double temp_y = pos_y;
		long temp_t = time;
		double temp_speed = speed;
		
		loadData(t);
		//System.out.println("time is " + Long.toString(time) + " and t is " + Long.toString(t));
		dest_pos_x = pos_x;
		dest_pos_y = pos_y;
		dest_coords_time = t;
		restoreIndex();
		
		direction = temp_dir;
		pos_x = temp_x;
		pos_y = temp_y;
		time = temp_t;
		speed = temp_speed;
	}
	
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
	public float getSoldier() {return soldier;}
	public void setId(int i){id=i;}
	public int getId(){return id;}
}