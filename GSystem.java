import java.util.*;

public class GSystem implements Positioning
{
	HashSet<Satellite> orbiting_objects;
	HashSet<Star> stars;
	String name;
	int x;
	int y;
	int navigability;
	
	int width;
	int height;
	
	//only used in the designer for multi-system drags
	int x_adj;
	int y_adj;
	
	public GSystem(int x, int y, String nm, HashSet<Satellite> orbiting, HashSet<Star> stars, int nav)
	{
		name=nm;
		orbiting_objects=orbiting;
		this.stars=stars;
		this.x=x;
		this.y=y;
		navigability=nav;
	}
	
	public HashSet<Double> getMassSet()
	{
		HashSet<Double> mass_set = new HashSet<Double>();
		for(Star st : stars)
			mass_set.add(st.getMass());
		
		return mass_set;
	}
	
	//methods required for save/load
	public GSystem(){}
	public HashSet<Satellite> getorbiting_objects(){return orbiting_objects;}
	public void setOrbiting_objects(HashSet<Satellite> s){orbiting_objects=s;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public HashSet<Star> getStars(){return stars;}
	public void setStars(HashSet<Star> st){stars=st;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
	public int getNavigability(){return navigability;}
	public void setNavigability(int nav){navigability=nav;}
	
	public int getWidth(){return width;}
	public void setWidth(int w){width=w;}
	public int getHeight(){return height;}
	public void setHeight(int h){height=h;}
	
	public int absoluteCurX(){return getWidth()/2;}
	public int absoluteCurY(){return getHeight()/2;}
	public int absoluteInitX(){return getWidth()/2;}
	public int absoluteInitY(){return getHeight()/2;}
}