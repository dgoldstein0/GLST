package galactic_strategy.game_objects;

import galactic_strategy.sync_engine.Describable;

public strictfp interface DescribableDestination<T extends DescribableDestination<T>> extends Describable<T>, AbstractDestination<T>
{

}