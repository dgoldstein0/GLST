public abstract class StellarObject implements Selectable
{
	int size;
	double mass;
	
	public StellarObject(){}
	public int getSize(){return size;}
	public void setSize(int sz){size=sz;}
	public double getMass(){return mass;}
	public void setMass(double d){mass=d;}
}