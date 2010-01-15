public class Focus implements Selectable
{
	private double x;
	private double y;
	Orbit owner;
	
	public Focus(double a, double b, Orbit o)
	{
		x=a;
		y=b;
		owner=o;
	}
	
	public Focus(){}
	public double getX(){return x;}
	public double getY(){return y;}
	public void setX(double a){x=a;}
	public void setY(double b){y=b;}
	public Orbit getOwner(){return owner;}
	public void setOwner(Orbit o){owner = o;}
}