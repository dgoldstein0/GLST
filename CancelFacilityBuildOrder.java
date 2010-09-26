import java.util.HashSet;
import java.util.Set;

public class CancelFacilityBuildOrder extends Order {

	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	int player_id;
	
	public CancelFacilityBuildOrder(OwnableSatellite<?> sat, long t)
	{
		the_sat=sat;
		player_id = sat.owner.getId();
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		scheduled_time=t;
		mode = Order.ORIGIN;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.NETWORK)
		{
			the_sat = sat_desc.retrieveObject(g, scheduled_time);
		}
		
		Set<Order> orders_to_reexecute = new HashSet<Order>();
		if(the_sat.owner.getId() == player_id) //verify that the player ordering this is the owner of the planet.
		{
			long cur_time = the_sat.time;
			if(cur_time > scheduled_time)
			{
				OwnableSatelliteDataSaver<?> data = the_sat.data_control.saved_data[the_sat.data_control.getIndexForTime(scheduled_time)];
				if(data.t_finish < cur_time)
				{
					//need to "recall" facility and everything it has done
					synchronized(the_sat.facilities_lock)
					{
						/*don't add these orders to the returned Set - we are, after all, canceling the facility,
						so all orders that go along with it thus become void*/
						
						Facility<?> the_fac = the_sat.facilities.get(data.next_fac_id);
						
						if(the_fac != null)
							the_fac.data_control.revertToTime(scheduled_time);
					}
				}
				orders_to_reexecute = the_sat.data_control.revertToTime(scheduled_time);
			}
			
			//the actual execution of the order.
			the_sat.cancelConstruction(scheduled_time, false); //(bldg_type, scheduled_time);
		}
		else
			orderDropped();
		return orders_to_reexecute;
	}
	
	public CancelFacilityBuildOrder(){mode = Order.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int i){player_id=i;}
}
