package galactic_strategy.game_objects;

import galactic_strategy.sync_engine.Saveable;

import java.util.HashSet;

public strictfp interface Targetable<T extends Targetable<T>> extends DescribableDestination<T>, Saveable<T>
{
	public abstract void addDamage(long t, int d);
	public abstract void destroyed(long t);
	public abstract HashSet<Targetter<?>> getAggressors();
	
	public abstract void addAggressor(Targetter<?> t);
	public abstract void removeAggressor(Targetter<?> t);
	public abstract boolean isAlive();
}