import java.util.HashSet;

public interface Targetable
{
	public abstract void addDamage(int d);
	public abstract void destroyed();
	public abstract HashSet<Targetter> getAggressors();
}