import java.util.*;

public strictfp interface Orbitable<T extends Orbitable<T>> extends Destination<T>
{
	public ArrayList<Satellite<?>> getOrbiting();
	public void setOrbiting(ArrayList<Satellite<?>> o);
	public abstract double absoluteCurX();
	public abstract double absoluteCurY();
	public abstract double absoluteInitX();
	public abstract double absoluteInitY();
	public abstract double getAbsVelX();
	public abstract double getAbsVelY();
	//public abstract HashSet<Double> getMassSet();
	public abstract double massSum();
}