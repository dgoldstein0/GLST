public strictfp class DestinationPoint implements Destination<DestinationPoint>, Describer<DestinationPoint>
{
	double x,y;
	
	public DestinationPoint(double x, double y)
	{
		this.x=x;
		this.y=y;
	}
	
	@Override
	public DestinationPoint retrieveObject(Galaxy g, long t){return this;}
	
	@Override
	public Describer<DestinationPoint> describer(){return this;}
	
	@Override
	public double getXCoord(long t){return x;}
	@Override
	public double getYCoord(long t){return y;}
	@Override
	public double getXVel(long t){return 0.0d;}
	@Override
	public double getYVel(long t){return 0.0d;}
	
	@Override
	public String imageLoc(){return "images/destinationpoint.jpg";}
	@Override
	public String getName(){return "Point";}
	
	//default constructor, for XML encoding/decoding
	public DestinationPoint(){}
	
	public double getX(){return x;}
	public void setX(double a){x=a;}
	public double getY(){return y;}
	public void setY(double b){y=b;}
}