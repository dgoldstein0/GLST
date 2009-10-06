import java.util.*;

interface Positioning
{
	public abstract int absoluteCurX();
	public abstract int absoluteCurY();
	public abstract int absoluteInitX();
	public abstract int absoluteInitY();
	public abstract HashSet<Double> getMassSet();
}