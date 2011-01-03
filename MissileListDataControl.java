import java.util.HashSet;
import java.util.Set;


public strictfp class MissileListDataControl extends RelaxedDataSaverControl<MissileList, MissileListDataSaver> {

	public MissileListDataControl(MissileList m) {
		super(m, new Creator<MissileList, MissileListDataSaver>(){
				public MissileListDataSaver create(){return new MissileListDataSaver();}
				public MissileListDataSaver[] createArray(){return new MissileListDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}

	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		
		Set<ReversionEffects.RevertObj> missiles_to_revert = new HashSet<ReversionEffects.RevertObj>();
			
		for(Missile.MissileId id : saved_data[indx].tbl.keySet())
		{
			missiles_to_revert.add(new ReversionEffects.RevertObj(saved_data[indx].tbl.get(id), saved_data[indx].t));
		}
		
		/* don't need to worry about missiles getting "put back in the missile launcher",
		 * b/c reverting a missile will revert its target and the target will revert all
		 * aggressors, including the shooter*/
		
		return new ReversionEffects(new HashSet<Order>(), missiles_to_revert);
	}

}
