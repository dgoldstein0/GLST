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
	
	@Override
	public int getSelectType(){return Selectable.FOCUS;}
	@Override
	public String generateName(){return "Focus of " + owner.obj.generateName();}
	@Override
	public int getTypeNumber(){return Selectable.FOCUS;}
	
	public Focus(){}
	public double getX(){return x;}
	public double getY(){return y;}
	public void setX(double a){x=a;}
	public void setY(double b){y=b;}
	public Orbit getOwner(){return owner;}
	public void setOwner(Orbit o){owner = o;}

	@Override
	public ImageResource getImage() {
		// TODO Auto-generated method stub
		return null;
	}
}