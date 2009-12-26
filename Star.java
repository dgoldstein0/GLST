public class Star extends StellarObject
{
	String name;
	int color;
	
	int x;
	int y;
	
	public Star(String name, int size, double m, int color, int x, int y)
	{
		this.name=name;
		this.size=size;
		this.color=color;
		this.x=x;
		this.y=y;
		mass=m;
	}
	
	//save/loading methods
	public Star(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public int getColor(){return color;}
	public void setColor(int c){color=c;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
}