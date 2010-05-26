public abstract class StellarObject implements Selectable
{
	double size;
	double mass;
	String name;
	
	public StellarObject(){}
	public double getSize(){return size;}
	public void setSize(double sz){size=sz;}
	public void setSize(int sz){size = (double)sz;} //for back-compatibility
	public double getMass(){return mass;}
	public void setMass(double d){mass=d;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
}