import java.util.Set;
import java.util.HashSet;

public class FleetDataSaverControl extends RelaxedDataSaverControl<Fleet, FleetDataSaver> {

	public FleetDataSaverControl(Fleet f) {
		super(f, new Creator<Fleet, FleetDataSaver >(){
			public FleetDataSaver create(){return new FleetDataSaver();}
			public FleetDataSaver[] createArray(){return new FleetDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
	
	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {

		/*two different groups of ships need to be reverted: the ones I'm reverting to, and those
		 * that need to "go back elsewhere" - i.e. into warp or into Shipyard queue*/
		
		Set<ReversionEffects.RevertObj> ships_to_revert = new HashSet<ReversionEffects.RevertObj>();
		for(Ship.ShipId id : saved_data[indx].ships.keySet())
			ships_to_revert.add(new ReversionEffects.RevertObj(saved_data[indx].ships.get(id), saved_data[indx].t));
		
		for(int i= indx, j=getNextIndex(i); j != index; i=j, j=getNextIndex(j))
		{
			for(Ship.ShipId id : saved_data[j].ships.keySet())
			{
				ReversionEffects.RevertObj obj = new ReversionEffects.RevertObj(saved_data[j].ships.get(id), saved_data[i].t);
				if(!ships_to_revert.contains(obj))
					ships_to_revert.add(obj);
			}
		}
		
		return new ReversionEffects(new HashSet<Order>(), ships_to_revert);
	}

}
