import java.util.HashSet;

public abstract class Flyer extends Targetter implements Targetable
{
	final static int data_capacity=50;
	
	ShipType type;
	GSystem location;
	String name;
	int id;
	
	HashSet<Targetter> aggressors; //all the ships/ missiles targetting this
	
	Destination destination;
	//save in case SystemPainter wants to paint crosshairs
	double dest_x_coord;
	double dest_y_coord;
	
	//for Targetable	
	int hull_strength;
	int damage;
	
	double pos_x; //pos_x and pos_y indicate where in the system the ship is located
	double pos_y;
	double direction;
	double speed;
	protected long time;
	FlyerAI current_flying_AI;
	
	ShipDataSaver ship_data[];
	int index;  //for the data array
	
	public Flyer(String nm, ShipType st)
	{
		name=nm;
		type=st;
		
		damage=0;
		hull_strength = st.hull;
		
		aggressors = new HashSet<Targetter>();
		ship_data=new ShipDataSaver[data_capacity];
		for(int i=0; i<ship_data.length; i++)
			ship_data[i] = new ShipDataSaver();
		index=0;
	}
	
	public Flyer(){}
	
	protected void advanceTime(long t)
	{
		long prev_time = time;
		time = (long)(Math.ceil((double)(t)/(double)(GalacticStrategyConstants.TIME_GRANULARITY))*GalacticStrategyConstants.TIME_GRANULARITY);
		if(time != prev_time)//otherwise, data already got saved 
			saveData();
	}
	
	//loading and saving data functions
	public void saveData()
	{
		//if(this instanceof Ship)
		//	System.out.println(Integer.toString(((Ship)this).id) + " saving time " + Long.toString(time) + " at index " + Integer.toString(index));
		
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
		int stepback=(int) (Math.floor((time-t)/GalacticStrategyConstants.TIME_GRANULARITY) + 1);
		//System.out.println("load data: t is " + Long.toString(t) + " and time is " + Long.toString(time) + ", so step back... " + Integer.toString(stepback));
		if (stepback>50)
		{
			System.out.println("Error loading ship data: the delay is too long");
		}
		else
		{
			if (stepback<=index)
				index-=stepback;
			else
				index+=data_capacity-stepback;			
		}
		//System.out.println(Integer.toString(index)+" "+Integer.toString(stepback));
		if (ship_data[index]!=null)
		{
			direction=ship_data[index].dir;
			pos_x=ship_data[index].px;
			pos_y=ship_data[index].py;
			time=ship_data[index].t;
			speed=ship_data[index].sp;
		}
		else System.out.println("Error loading ship data: data wasn't saved");	//BOOKMARK - we need a different way to detect this.  Or is this error even possible?
	}
	
	private int saveindex;
	
	public void restoreIndex()
	{
		index=saveindex;
	}
	
	//ship physics functions
	
	//moves the ship one time_granularity.  this is a separate function so that all ships updates can be stepped through 1 by 1.
	protected void moveIncrement()
	{
		//change position
		pos_x += speed*GalacticStrategyConstants.TIME_GRANULARITY*Math.cos(direction);
		pos_y += speed*GalacticStrategyConstants.TIME_GRANULARITY*Math.sin(direction);
		
		double desired_direction_chng = current_flying_AI.calcDesiredDirection();
		double desired_speed = current_flying_AI.calcDesiredSpeed(desired_direction_chng);
		
		//change speed
		if(desired_speed < speed)
			speed = Math.max(Math.max(speed - type.accel_rate*GalacticStrategyConstants.TIME_GRANULARITY, desired_speed), 0.0d);
		else
			speed = Math.min(Math.min(speed + type.accel_rate*GalacticStrategyConstants.TIME_GRANULARITY, desired_speed), type.max_speed);
		
		//change direction
		double actual_chng = Math.min(Math.abs(desired_direction_chng), Math.abs(type.max_angular_vel*GalacticStrategyConstants.TIME_GRANULARITY)); //finds the absolute value of the amount the direction changes
		if(desired_direction_chng > 0)
			direction += actual_chng;
		else
			direction -= actual_chng;
		
		if(direction > Math.PI)
			direction -= 2*Math.PI;
		else if(direction<-Math.PI)
			direction += 2*Math.PI;
	}
	
	public double destinationX()
	{
		dest_x_coord = destination.getXCoord(time);
		return dest_x_coord;
	}
	
	public double destinationY()
	{
		dest_y_coord = destination.getYCoord(time);
		return dest_y_coord;
	}
	
	public double destinationVelX()
	{
		return destination.getXVel(time);
	}
	
	public double destinationVelY()
	{
		return destination.getYVel(time);
	}
	
	//methods to implement destination
	public String imageLoc(){return type.getImg_loc();}
	
	private long dest_coords_time=0; //there cannot be a call to getXCoord or getYCoord with t=0, since ships do not exist then, and if they do will not be targetting other ships
	private double dest_pos_x;
	private double dest_pos_y;
	private double dest_vel_x;
	private double dest_vel_y;
	
	public double getXCoord(long t)
	{
		if(t != dest_coords_time)		
			updateDestData(t);
		return dest_pos_x;
	}
	
	public double getYCoord(long t)
	{
		if(t != dest_coords_time)
			updateDestData(t);
		return dest_pos_y;
	}
	
	public double getXVel(long t)
	{
		if(t != dest_coords_time)
			updateDestData(t);
		return dest_vel_x;
	}
	
	public double getYVel(long t)
	{
		if(t != dest_coords_time)
			updateDestData(t);
		return dest_vel_y;
	}
	
	private void updateDestData(long t)
	{
		double temp_dir = direction;
		double temp_x = pos_x;
		double temp_y = pos_y;
		long temp_t = time;
		double temp_speed = speed;
		
		loadData(t);
		//System.out.println("TIME is " + Long.toString(time) + " and t is " + Long.toString(t));
		dest_pos_x = pos_x;
		dest_pos_y = pos_y;
		dest_vel_x = speed*Math.cos(direction);
		dest_vel_y = speed*Math.sin(direction);
		dest_coords_time = t;
		restoreIndex();
		
		direction = temp_dir;
		pos_x = temp_x;
		pos_y = temp_y;
		time = temp_t;
		speed = temp_speed;
	}
	
	//for Targetable
	public HashSet<Targetter> getAggressors(){return aggressors;}
	public void addAggressor(Targetter t){aggressors.add(t);}
	public void removeAggressor(Targetter t){aggressors.remove(t);}
	
	public void addDamage(int d)
	{
		damage+=d;
		if(damage>=hull_strength)
			destroyed();
	}
	
	public ShipType getType(){return type;}
	public void setType(ShipType tp){type=tp;}
	public double getPos_x(){return pos_x;}
	public double getPos_y(){return pos_y;}
	public void setPos_x(double x){pos_x=x;}
	public void setPos_y(double y){pos_y=y;}
	public double getSpeed(){return speed;}
	public double getDirection(){return direction;}
	public void setSpeed(double s){speed=s;}
	public void setDirection(double d){direction=d;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public int getHull_strength(){return hull_strength;}
	public void setHull_strength(int hs){hull_strength=hs;}
	public int getId(){return id;}
	public void setId(int i){id=i;}
}