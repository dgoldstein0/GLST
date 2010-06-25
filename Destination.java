public interface Destination<T extends Destination<T>> extends Describable<T>
{
	public abstract double getXCoord(long t);
	public abstract double getYCoord(long t);
	public abstract double getXVel(long t);
	public abstract double getYVel(long t);
	
	public abstract String imageLoc();
	public abstract String getName();
}