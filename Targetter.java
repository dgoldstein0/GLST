public abstract class Targetter<T extends Targetter<T>> implements Targetable<T>
{
	
	Targetable<?> target;
	
	public Targetable<?> getTarget(){return target;}
	public void setTarget(Targetable<?> s){target = s;}
	
	public abstract void targetIsDestroyed(long time);
	public abstract void targetHasWarped(long time);
}