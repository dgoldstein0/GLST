import java.util.HashSet;

public interface Targetable<T extends Targetable<T>> extends Destination<T>
{
	public abstract void addDamage(int d);
	public abstract void destroyed();
	public abstract HashSet<Targetter> getAggressors();
	public abstract void addAggressor(Targetter t);
	public abstract void removeAggressor(Targetter t);
}