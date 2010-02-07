public class Star extends StellarObject
{
	String name; //should be a greek letter, such as Alpha, Beta, Gamma, Delta, etc.
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
	
	public String generateName()
	{
		if(name != "")
			return owner.name + " " + name;
		else
			return owner.name;
	}
	
	//save/loading methods
	public Star(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public GSystem getOwner(){return owner;}
	public void setOwner(GSystem o){owner=o;}
	public int getColor(){return color;}
	public void setColor(int c){color=c;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
}