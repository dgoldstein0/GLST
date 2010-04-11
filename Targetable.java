import java.util.HashSet;

public interface Targetable extends Destination
{
	public abstract void addDamage(int d);
	public abstract void destroyed();
	public abstract HashSet<Targetter> getAggressors();
	public abstract void addAggressor(Targetter t);
}