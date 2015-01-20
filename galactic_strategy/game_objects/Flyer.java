package galactic_strategy.game_objects;

import galactic_strategy.Constants;
import galactic_strategy.Player;

import java.util.HashSet;
import java.util.Iterator;

public strictfp abstract class Flyer<T extends Flyer<T,ID,ITERATOR>, ID extends Flyer.FlyerId<ID>, ITERATOR extends Iterator<ID>> extends Targetter<T>
{
	//these two constants are used by loadData for its second argument, to
	//instruct it whether or not to call loadMoreData from the FlyerDataSaver (or subclass thereof)
	//which it finds
	final static int ONLY_FLYER_DATA=0;
	final static int ALL_DATA=1;
	
	ShipType type;
	GSystem location;
	String name;
	ID id;
	Player owner;
	
	//for Targetable
	int damage;
	boolean is_alive;
	HashSet<Targetter<?>> aggressors; //all the ships/ missiles targeting this
	
	public FlyingThing flying_part;
	
	//subclasses are responsible for instantiating data_control
	public Flyer(String nm, ShipType st)
	{
		super();
		name=nm;
		type=st;
		
		damage=0;
		is_alive=true;
		aggressors = new HashSet<Targetter<?>>();
		target=null;
		flying_part = new FlyingThing(st.getCapabilities(this));
	}
	
	public Flyer(){}
	
	public abstract void removeFromGame();
	public boolean isAlive(){return is_alive;}
	public abstract boolean update(long time, ITERATOR iterator);
	
	//ship physics functions
	
	
	protected boolean enforceSpeedCap(){return true;} //overriding this function allows ships to speed up to warp.  note that if it were Private it would not be overrideable
	
	protected double getAccel()
	{
		return type.accel_rate;
	}
	
	public double destinationX()
	{
		return flying_part.destination.getXCoord();
	}
	
	public double destinationY()
	{
		return flying_part.destination.getYCoord();
	}
	
	//methods to implement destination
	@Override
	public String imageLoc(){return type.img.img_path;}
	
	@Override public double getXCoord() {return flying_part.getPos_x();}
	@Override public double getYCoord() {return flying_part.getPos_y();}
	
	@Override
	public double getXVel()
	{
		return flying_part.getSpeed()*Math.cos(flying_part.getDirection());
	}
	
	@Override
	public double getYVel()
	{
		return flying_part.getSpeed()*Math.sin(flying_part.getDirection());
	}
	
	//for Targetable
	public HashSet<Targetter<?>> getAggressors(){return aggressors;}
	public void addAggressor(Targetter<?> t)
	{
		aggressors.add(t);
	}
	public void removeAggressor(Targetter<?> t)
	{
		aggressors.remove(t);
	}
	
	@Override
	public void addDamage(long t, int d)
	{
		damage+=d;
		if(damage>=type.hull)
			destroyed(t);
	}
	
	public ShipType getType(){return type;}
	public void setType(ShipType tp){type=tp;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public int getDamage(){return damage;}
	public void setDamage(int d){damage=d;}
	public void setId(ID i){id=i;}
	public ID getId(){return id;}
	public Player getOwner() {return owner;}
	public void setOwner(Player p){owner = p;}
	public boolean getIs_alive() {return is_alive;}
	public void setIs_alive(boolean b) {is_alive = b;}
	public GSystem getLocation() {return location;}
	public void setLocation(GSystem sys) {location = sys;}
	public FlyingThing getFlyingPart() {return flying_part;}
	public void setFlyingPart(FlyingThing f){flying_part = f;}

	public static abstract class FlyerId<T extends FlyerId<T>>
	{
		@Override
		public abstract int hashCode();
		
		@Override
		public abstract boolean equals(Object o);
	}

	// TODO should this be part of FlyingThing?
	/**
	 * Things that don't warp should always return false.
	 */
	public abstract boolean isInWarpTransition();
}