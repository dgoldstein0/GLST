public interface Destination
{
	public abstract double getXCoord(long t);
	public abstract double getYCoord(long t);
	
	public abstract String imageLoc();
	public abstract String getName();
}