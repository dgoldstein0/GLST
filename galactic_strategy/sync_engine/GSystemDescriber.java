package galactic_strategy.sync_engine;

import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.Galaxy;

public strictfp class GSystemDescriber implements Describer<GSystem>
{
	int id;
	
	public GSystemDescriber(GSystem sys)
	{
		id=sys.getId();
	}
	
	@Override
	public GSystem retrieveObject(Galaxy g)
	{
		return g.getSystems().get(id);
	}
	
	public GSystemDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
}