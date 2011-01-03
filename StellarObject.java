public strictfp abstract class StellarObject implements Selectable
{
	double size;
	double mass;
	String name;
	int picture_num; //indexes into the ImageResource enum
	
	public StellarObject(){}
	public double getSize(){return size;}
	public void setSize(double sz){size=sz;}
	public void setSize(int sz){size = (double)sz;} //for back-compatibility
	public double getMass(){return mass;}
	public void setMass(double d){mass=d;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public int getPicture_num(){return picture_num;}
	public void setPicture_num(int num){picture_num = num;}
	
	@Override
	public ImageResource getImage(){return ImageResource.values()[picture_num];}
}