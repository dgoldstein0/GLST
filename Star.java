public class Star extends StellarObject
{
	final static int COLOR_NULL=0;
	final static int COLOR_RED=1;
	final static int COLOR_ORANGE=2;
	final static int COLOR_YELLOW=3;
	final static int COLOR_WHITE=4;
	final static int COLOR_BLUE=5;
	
	//stores image URLs
	final static String[] color_choice={"images/null.png","images/red.png","images/orange.png","images/yellow.png","images/white.png","images/blue.png"};
	
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