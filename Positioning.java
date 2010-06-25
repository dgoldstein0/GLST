interface Positioning<T extends Positioning<T>> extends Destination<T>
{
	public abstract double absoluteCurX();
	public abstract double absoluteCurY();
	public abstract double absoluteInitX();
	public abstract double absoluteInitY();
	public abstract double getAbsVelX();
	public abstract double getAbsVelY();
	//public abstract HashSet<Double> getMassSet();
	public abstract double massSum();
}