public interface Destination extends Describable
{
	public abstract double getXCoord(long t);
	public abstract double getYCoord(long t);
	public abstract double getXVel(long t);
	public abstract double getYVel(long t);
	
	public abstract String imageLoc();
	public abstract String getName();
}