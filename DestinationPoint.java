public class DestinationPoint implements Destination
{
	double x,y;
	
	public DestinationPoint(double x, double y)
	{
		this.x=x;
		this.y=y;
	}
	
	public double getXCoord(long t){return x;}
	public double getYCoord(long t){return y;}
}