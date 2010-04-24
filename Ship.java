import java.util.HashSet;

public class Ship extends Flyer implements Selectable
{
	Player owner;
	int id;
	
	int energy;
	int max_energy;
	long nextAttackingtime;
	
	float soldier;
	boolean attacking;
	
	public Ship(String nm, ShipType t, int id)
	{
		this.id=id;
		energy = t.max_energy;
		max_energy = energy;
		soldier=t.soldier_capacity;//assume ships are fully loaded when built
		nextAttackingtime=0;
		SetUpFlyer(nm, t);
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
		saveData();
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
		System.out.println("t is " + Long.toString(t) + " and time is " + Long.toString(time));
		dest_x_coord = d.getXCoord(time-time_granularity);
		dest_y_coord = d.getYCoord(time-time_granularity);
		current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.LANDING_RANGE, true);
		
		attacking = false;
		//current_flying_AI = new PatrolAI(this, 400.0, 300.0, 100.0, 1);
	}
	
	public void update(long t)
	{
		move(t);		
		if(attacking)
		{
			attack(t);
		}
	}
	
	public void orderToAttack(long t, Targetable tgt)
	{
		target= tgt;
		attacking=true;
		target.addAggressor(this);
		nextAttackingtime = (long)(Math.ceil((double)(t)/(double)(time_granularity))*time_granularity);
		nextAttackingtime+=GalacticStrategyConstants.Attacking_cooldown;
		
		current_flying_AI = new TrackingAI(this, GalacticStrategyConstants.Attacking_Range-5, true);
	}
	
	public void attack(long t)
	{
		double dx = destinationX() - pos_x;
		double dy = destinationY() - pos_y;
		if ((dx*dx+dy*dy<GalacticStrategyConstants.Attacking_Range*GalacticStrategyConstants.Attacking_Range)&&(nextAttackingtime<=t))
		{
			Missile m=new Missile(this, target, t); 
			synchronized(location.missile_lock)
			{
				location.missiles.add(m);
			}
			nextAttackingtime+=GalacticStrategyConstants.Attacking_cooldown;
		}
	}
	
	
	public void destroyed()
	{
		System.out.println("destroyed-before");	
		if(location.fleets[owner.getId()].remove(this))//in case another attack has already destroyed the ship, but both call the destroyed method
		{
			//notify aggressors
			for(Targetter t : aggressors)
				t.targetIsDestroyed();
			
			//notify interface
			if(this == GameInterface.GC.GI.ShipPanel.the_ship)
			{
				GameInterface.GC.GI.displayNoPanel();
				GameInterface.GC.GI.selected_in_sys = null;
			}			
			System.out.println("destroyed-after");
		}
	}
	
	public void targetIsDestroyed()
	{
		attacking=false;
		if (destination==target)
		{
			destination=new DestinationPoint(target.getXCoord(time),target.getYCoord(time));
		}
	}
	
	public int getSoldierInt(){return (int)Math.floor(soldier);}
	
	//methods required for save/load
	public Ship(){}
	public int getEnergy(){return energy;}
	public void setEnergy(int f){energy=f;}
	public int getMax_energy(){return max_energy;}
	public void setMax_energy(int mf){max_energy=mf;}
	public Player getOwner() {return owner;}
	public float getSoldier() {return soldier;}
	public void setId(int i){id=i;}
	public int getId(){return id;}
}