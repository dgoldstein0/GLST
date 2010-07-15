import java.util.Hashtable;


public class ShipyardDataSaver extends FacilityDataSaver<Shipyard> {
	long time_on_cur_s;
	Hashtable<Integer, Ship> queue;
	double as_x;
	double as_y;
	int next_q_id;
	
	public ShipyardDataSaver()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadData(Shipyard s) {
		super.loadData(s);

		s.time_on_current_ship = time_on_cur_s;
		s.manufac_queue = (Hashtable<Integer,Ship>) queue.clone(); //generates an unchecked type cast warning
		s.assemble_x = as_x;
		s.assemble_y = as_y;
		s.next_queue_id = next_q_id;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSaveMoreData(Shipyard s) {

		time_on_cur_s = s.time_on_current_ship;
		queue = (Hashtable<Integer, Ship>) s.manufac_queue.clone(); //unchecked type cast warning
		as_x = s.assemble_x;
		as_y = s.assemble_y;
		next_q_id = s.next_queue_id;
	}

}
