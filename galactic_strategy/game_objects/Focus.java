package galactic_strategy.game_objects;

import galactic_strategy.ui.ImageResource;
import galactic_strategy.ui.Selectable;

public strictfp class Focus implements Selectable
{
	private double x;
	private double y;
	Orbit owner;
	
	public Focus(double a, double b, Orbit o)
	{
		x=a;
		y=b;
		owner=o;
	}
	
	@Override
	public int getSelectType(){return Selectable.FOCUS;}
	@Override
	public String generateName(){return "Focus of " + owner.obj.generateName();}
	
	public Focus(){}
	public double getX(){return x;}
	public double getY(){return y;}
	public void setX(double a){x=a;}
	public void setY(double b){y=b;}
	public Orbit getOwner(){return owner;}
	public void setOwner(Orbit o){owner = o;}

	@Override
	public ImageResource getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean shouldSelect(double x, double y, double tolerance) {
		Orbitable<?> boss = owner.getBoss();
		double boss_x = boss.absoluteCurX();
		double boss_y = boss.absoluteCurY();
		
		return this.x + boss_x - tolerance <= x &&
				this.x <= this.x + boss_x + tolerance &&
				this.y + boss_y - tolerance <= y &&
				y <= this.y + boss_y + tolerance;
	}
}