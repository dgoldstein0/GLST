import java.util.HashSet;

public interface Targetable<T extends Targetable<T>> extends Destination<T>, Saveable<T>
{
	public abstract void addDamage(long t, int d);
	public abstract void destroyed();
	public abstract HashSet<Targetter<?>> getAggressors();
	
	public abstract void addAggressor(Targetter<?> t);
	public abstract void removeAggressor(Targetter<?> t);
	public abstract boolean isAlive();
	public abstract boolean isAliveAt(long t) throws DataSaverControl.DataNotYetSavedException;
}