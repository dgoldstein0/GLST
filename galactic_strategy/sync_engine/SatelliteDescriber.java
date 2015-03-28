package galactic_strategy.sync_engine;

import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.Orbitable;
import galactic_strategy.game_objects.Satellite;

public strictfp class SatelliteDescriber<T extends Satellite<T>> implements Describer<T>
{
	int id;
	Describer<? extends Orbitable<?>> boss_describer;
	
	public SatelliteDescriber(Satellite<T> sat)
	{
		id=sat.getId();
		boss_describer = sat.getOrbit().getBoss().describer();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T retrieveObject(Galaxy g)
	{
		Orbitable<?> boss = boss_describer.retrieveObject(g);
		
		return (T)boss.getOrbiting().get(id); //orbiting is only an array of Satellites, so, though we know here we will have a T, this gives an unchecked exception
	}
	
	public SatelliteDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public Describer<? extends Orbitable<?>> getBoss_describer(){return boss_describer;}
	public void setBoss_describer(Describer<? extends Orbitable<?>> b){boss_describer=b;}
}