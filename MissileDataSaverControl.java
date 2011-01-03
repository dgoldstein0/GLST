import java.util.Set;
import java.util.HashSet;

public strictfp class MissileDataSaverControl extends FlyerDataSaverControl<Missile, FlyerDataSaver<Missile>> {

	@SuppressWarnings("unchecked")
	public MissileDataSaverControl(Missile m)
	{
		super(m, new Creator<Missile, FlyerDataSaver<Missile> >(){
				public FlyerDataSaver<Missile> create(){return new FlyerDataSaver<Missile>();}
				public FlyerDataSaver[] createArray(){return new FlyerDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}

	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		//no orders for missiles... yet.  But if it gets rolled back, target should be too, as should aggressors
		Set<ReversionEffects.RevertObj> effected_objs = new HashSet<ReversionEffects.RevertObj>();
		
		//note assumption here that the target never changes
		effected_objs.add(new ReversionEffects.RevertObj((Saveable<?>) saved_data[indx].tgt, saved_data[indx].t));
		
		for(int j=getNextIndex(indx); j != index; j=getNextIndex(j))
		{
			for(Targetter<?> t : saved_data[j].aggr)
			{
				if(!effected_objs.contains(t))
					effected_objs.add(new ReversionEffects.RevertObj(t, saved_data[j].t));
			}
		}
		
		return new ReversionEffects(new HashSet<Order>(), effected_objs);
	}
}
