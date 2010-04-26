public abstract class Targetter
{
	
	Targetable target;
	
	public Targetable getTarget(){return target;}
	public void setTarget(Targetable s){target = s;}
	
	public abstract void targetIsDestroyed(long time);
}