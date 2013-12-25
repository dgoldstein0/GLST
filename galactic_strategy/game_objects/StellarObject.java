package galactic_strategy.game_objects;

import galactic_strategy.ui.ImageResource;
import galactic_strategy.ui.Selectable;

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
	public abstract double getX();
	public abstract double getY();
	
	@Override
	public ImageResource getImage(){return ImageResource.values()[picture_num];}
	
	@Override
	public boolean shouldSelect(double x, double y, double tolerance) {
		double obj_x = getX();
		double obj_y = getY();
		double size = getSize();
		return obj_x - size/2 <= x - tolerance && x <= obj_x + size/2 + tolerance &&
				obj_y - size/2 <= y - tolerance && y <= obj_y + size/2 + tolerance;
	}
}