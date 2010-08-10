import java.util.HashSet;
import java.util.Set;

public class OwnableSatelliteDataSaverControl<T extends OwnableSatellite<T>> extends RelaxedDataSaverControl<T, OwnableSatelliteDataSaver<T>> {
	public OwnableSatelliteDataSaverControl(T sat) {
		super(sat, new Creator<T,  OwnableSatelliteDataSaver<T>>(){
			public OwnableSatelliteDataSaver<T> create(){return new OwnableSatelliteDataSaver<T>();}
			public OwnableSatelliteDataSaver<T>[] createArray(){return new OwnableSatelliteDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}

	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		
		Set<Order> orders = new HashSet<Order>();
		Set<ReversionEffects.RevertObj> objs = new HashSet<ReversionEffects.RevertObj>();
		for(int id: saved_data[indx].fac.keySet())
		{
			objs.add(new ReversionEffects.RevertObj(saved_data[indx].fac.get(id), saved_data[indx].t));
		}
		
		for(int i = indx, j=getNextIndex(i); j != index; i=j, j=getNextIndex(j))
		{
			//list all facilities
			for(int id : saved_data[j].fac.keySet())
			{
				//role back facility to before it existed
				ReversionEffects.RevertObj obj = new ReversionEffects.RevertObj(saved_data[j].fac.get(id), saved_data[j].t-1);
				if(!objs.contains(obj))
					objs.add(obj);
			}
			
			//can tell if something is built or canceled via this condition
			if(saved_data[i].bldg_in_prog != saved_data[j].bldg_in_prog)
			{
				//if we did NOT just finish the building
				if(!(saved_data[j].bldg_in_prog == FacilityType.NO_BLDG && saved_data[j].t == saved_data[j].t_finish))
				{
					//building was canceled
					orders.add(new CancelFacilityBuildOrder(the_obj, saved_data[j].t));
				}
				else if(saved_data[i].bldg_in_prog == FacilityType.NO_BLDG)
				{
					//began to build a facility
					orders.add(new FacilityBuildOrder(the_obj, saved_data[j].bldg_in_prog, saved_data[j].t_start));
				}
			}
		}
		
		return new ReversionEffects(orders, objs);
	}
	
	protected void doReversionPrep(int indx)
	{
		for(int i = getNextIndex(indx); i != index; i=getNextIndex(i))
		{
			saved_data[i].own.changeMoney(-saved_data[i].mon_added);
		}
	}
}
