public class Star extends StellarObject
{
	int color;
	GSystem owner;
	
	int x;
	int y;
	
	public Star(String name, int size, double m, int color, int x, int y, GSystem o)
	{
		this.name=name;
		this.size=size;
		this.color=color;
		this.x=x;
		this.y=y;
		mass=m;
		owner=o;
	}
	
	//for Selectable, implemented by StellarObject
	public String generateName()
	{
		if(name != "")
			return owner.name + " " + name;
		else
			return owner.name;
	}
	
	public int getSelectType(){return Selectable.STAR;}
	
	//save/loading methods
	public Star(){}
	public GSystem getOwner(){return owner;}
	public void setOwner(GSystem o){owner=o;}
	public int getColor(){return color;}
	public void setColor(int c){color=c;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
}