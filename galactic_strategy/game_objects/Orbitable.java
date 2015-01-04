package galactic_strategy.game_objects;

import java.util.List;

/**Note that the comparison operators of the subclasses are
 * not very well abstracted from the fact that other classes
 * can inherit from this interface*/
public strictfp interface Orbitable<T extends Orbitable<T>> extends Comparable<Orbitable<?>>, DescribableDestination<T>
{
	public List<Satellite<?>> getOrbiting();
	public void setOrbiting(List<Satellite<?>> o);
	public abstract double absoluteCurX();
	public abstract double absoluteCurY();
	public abstract double absoluteInitX();
	public abstract double absoluteInitY();
	public abstract double getAbsVelX();
	public abstract double getAbsVelY();
	//public abstract HashSet<Double> getMassSet();
	public abstract double massSum();
}