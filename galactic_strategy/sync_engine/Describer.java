package galactic_strategy.sync_engine;

import galactic_strategy.game_objects.Galaxy;

public strictfp interface Describer<T>
{
	public abstract T retrieveObject(Galaxy g);
}