import java.util.*;

public class GSystem implements Positioning
{
	HashSet<Satellite> orbiting_objects;
	HashSet<Star> stars;
	Fleet[] fleets; //indices = player id's
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
		fleets = new Fleet[GalacticStrategyConstants.MAX_PLAYERS];
	}
	
	//this seems to not work right.  I don't know why.  so it is being replaced by massSum
	/*public HashSet<Double> getMassSet()
	{
		HashSet<Double> mass_set = new HashSet<Double>();
		if(stars instanceof HashSet)
		{
			for(Star st : stars)
				mass_set.add(st.getMass());
		}
		
		return mass_set;
	}*/
	
	public double massSum()
	{
		double sum=0;
		if(stars instanceof HashSet)
		{
			for(Star st : stars)
				sum += st.getMass();
		}
		return sum;
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
	public Fleet[] getFleets(){return fleets;}
	public void setFleets(Fleet[] f){fleets=f;}
	
	public int getWidth(){return width;}
	public void setWidth(int w){width=w;}
	public int getHeight(){return height;}
	public void setHeight(int h){height=h;}
	
	public double absoluteCurX(){return ((double)getWidth())/2;}
	public double absoluteCurY(){return ((double)getHeight())/2;}
	public double absoluteInitX(){return ((double)getWidth())/2;}
	public double absoluteInitY(){return ((double)getHeight())/2;}
}