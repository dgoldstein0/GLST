package galactic_strategy.sync_engine;

import galactic_strategy.game_objects.Facility;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;

public strictfp class FacilityDescriber<T extends Facility<T>> implements Describer<T>
{
	int id;
	Describer<? extends OwnableSatellite<?>> boss;
	
	public FacilityDescriber(Facility<T> f)
	{
		id = f.getId();
		boss = f.getLocation().describer();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T retrieveObject(Galaxy g)
	{
		return (T) boss.retrieveObject(g).getFacilities().get(id);
	}
	
	public FacilityDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public Describer<? extends OwnableSatellite<?>> getBoss(){return boss;}
	public void setBoss(Describer<? extends OwnableSatellite<?>> b){boss=b;}
}