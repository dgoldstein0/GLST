import java.util.HashSet;
import java.util.Set;

public strictfp class CancelFacilityBuildOrder extends Order {

	SatelliteDescriber<? extends OwnableSatellite<?>> sat_desc;
	OwnableSatellite<?> the_sat;
	
	public CancelFacilityBuildOrder(OwnableSatellite<?> sat, long t)
	{
		super(t, sat.owner);
		the_sat=sat;
		sat_desc = (SatelliteDescriber<? extends OwnableSatellite<?>>)sat.describer();
		mode = Order.MODE.ORIGIN;
	}
	
	public Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		if(mode==Order.MODE.NETWORK)
		{
			the_sat = sat_desc.retrieveObject(g, scheduled_time);
		}
		
		Set<Order> orders_to_reexecute = new HashSet<Order>();
		if(the_sat.owner.getId() == p_id) //verify that the player ordering this is the owner of the planet.
		{
			long cur_time = the_sat.time;
			if(cur_time > scheduled_time)
			{
				OwnableSatelliteDataSaver<?> data = the_sat.data_control.saved_data[the_sat.data_control.getIndexForTime(scheduled_time)];
				if(data.t_finish < cur_time)
				{
					//need to "recall" facility and everything it has done
					synchronized(the_sat.facilities)
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
			the_sat.cancelConstruction(scheduled_time); //(bldg_type, scheduled_time);
		}
		else
			orderDropped();
		return orders_to_reexecute;
	}
	
	public CancelFacilityBuildOrder(){mode = Order.MODE.NETWORK;}
	public SatelliteDescriber<? extends OwnableSatellite<?>> getSat_desc(){return sat_desc;}
	public void setSat_desc(SatelliteDescriber<? extends OwnableSatellite<?>> s){sat_desc=s;}
}
