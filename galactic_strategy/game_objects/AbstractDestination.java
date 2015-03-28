package galactic_strategy.game_objects;

public interface AbstractDestination<T extends AbstractDestination<T>> {
	public abstract double getXCoord();
	public abstract double getYCoord();
	public abstract double getXVel();
	public abstract double getYVel();
	
	public abstract String imageLoc();
	public abstract String getName();
}
