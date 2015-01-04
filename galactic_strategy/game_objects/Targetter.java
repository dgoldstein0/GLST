package galactic_strategy.game_objects;

import galactic_strategy.sync_engine.DataSaverControl;

public strictfp abstract class Targetter<T extends Targetter<T>> implements Targetable<T>
{
	Targetable<?> target;
	DataSaverControl<T> data_control;
	
	public Targetable<?> getTarget(){return target;}
	public void setTarget(Targetable<?> s){target = s;}
	
	public abstract void targetIsDestroyed(long t);
	public abstract void targetHasWarped(long t);
	
	//for Saveable
	public DataSaverControl<T> getDataControl(){return data_control;}
	public Targetter(){
		data_control = new DataSaverControl<T>((T) this);
	}
}