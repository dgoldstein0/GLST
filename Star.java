public class Star extends StellarObject
{
	GSystem owner;
	
	int x;
	int y;
	
	public Star(String name, int size, double m, int color, int x, int y, GSystem o)
	{
		this.name=name;
		this.size=size;
		this.picture_num=color;
		this.x=x;
		this.y=y;
		mass=m;
		owner=o;
	}
	
	//for Selectable, implemented by StellarObject
	@Override
	public int getTypeNumber()
	{
		return Selectable.STAR | (picture_num << 2);
	}
	
	@Override
	public String generateName()
	{
		if(name != "")
			return owner.name + " " + name;
		else
			return owner.name;
	}
	
	@Override
	public int getSelectType(){return Selectable.STAR;}
	
	//save/loading methods
	public Star(){}
	public GSystem getOwner(){return owner;}
	public void setOwner(GSystem o){owner=o;}
	@Deprecated
	public void setColor(int c){picture_num=c;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
}