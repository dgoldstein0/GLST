package galactic_strategy.game_objects;

import galactic_strategy.sync_engine.Describable;

public strictfp interface Destination<T extends Destination<T>> extends Describable<T>
{
	public abstract double getXCoord();
	public abstract double getYCoord();
	public abstract double getXVel();
	public abstract double getYVel();
	
	public abstract String imageLoc();
	public abstract String getName();
}