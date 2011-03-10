import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;

public strictfp class ShipyardDataSaverControl extends FacilityDataSaverControl<Shipyard, ShipyardDataSaver> {
	
	public ShipyardDataSaverControl(Shipyard s) {
		super(s, new Creator<Shipyard, ShipyardDataSaver >(){
			public ShipyardDataSaver create(){return new ShipyardDataSaver();}
			public ShipyardDataSaver[] createArray(){return new ShipyardDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}

	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		
		Set<Order> orders = new HashSet<Order>();
		Set<ReversionEffects.RevertObj> objs = new HashSet<ReversionEffects.RevertObj>();
		
		for(int i=indx, j=getNextIndex(i); j!= index; i=j, j=getNextIndex(j))
		{
			addAggressorsAtIndex(j, objs);
			
			if(!saved_data[i].queue.equals(saved_data[j].queue))
			{
				//why are the queue's different?
				Set<Integer> old_remove_new = ((Hashtable<Integer, Ship>)saved_data[i].queue.clone()).keySet();
				old_remove_new.removeAll(saved_data[j].queue.keySet());
				
				//anything in this set was canceled... or finished
				for(Integer k : old_remove_new)
				{
					Ship ship_in_prog = saved_data[i].queue.get(Collections.min(saved_data[i].queue.keySet()));
					
					if(saved_data[j].t - saved_data[i].t + saved_data[i].time_on_cur_s <= ship_in_prog.type.time_to_build) //if an order lands on the time grain, it would get done, hence "<="
						orders.add(new ShipyardCancelBuildOrder(the_obj, saved_data[i].queue.get(k), saved_data[i].t));
					else
						objs.add(new ReversionEffects.RevertObj(ship_in_prog, saved_data[j].t-1));
				}
				
				Set<Integer> new_remove_old = ((Hashtable<Integer, Ship>)saved_data[j].queue.clone()).keySet();
				new_remove_old.removeAll(saved_data[i].queue.keySet());
				
				//anything in this set was ordered to be built
				for(Integer k : new_remove_old)
				{
					orders.add(new ShipyardBuildShipOrder(the_obj, saved_data[j].queue.get(k).type, saved_data[j].t)); //assumption that the order happened on the time grain
				}
			}
		}
		
		return new ReversionEffects(orders, objs);
	}
}
