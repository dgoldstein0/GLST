import java.util.Hashtable;


public class ShipyardDataSaver extends FacilityDataSaver<Shipyard> {
	long time_on_cur_s;
	Hashtable<Integer, Ship> queue;
	double as_x;
	double as_y;
	
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
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveData(Shipyard s) {
		super.saveData(s);

		time_on_cur_s = s.time_on_current_ship;
		queue = (Hashtable<Integer, Ship>) s.manufac_queue.clone(); //unchecked type cast warning
		as_x = s.assemble_x;
		as_y = s.assemble_y;
	}

}
