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
	public double getXVel(long t){return 0.0d;}
	public double getYVel(long t){return 0.0d;}
	
	public String imageLoc(){return "images/destinationpoint.jpg";}
	public String getName(){return "Point";}
	
	public double getX(){return x;}
	public void setX(double a){x=a;}
	public double getY(){return y;}
	public void setY(double b){y=b;}
}