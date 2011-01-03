import java.util.Hashtable;


public strictfp class OwnableSatelliteDataSaver<T extends OwnableSatellite<T>> extends DataSaver<T> {

	boolean is_data_saved;
	FacilityType bldg_in_prog;
	long t_finish;
	long t_start;
	int next_fac_id;
	long last_tax_t;
	Player own;
	Hashtable<Integer, Facility<?>> fac; //save this in case facility gets destroyed, so we still have a reference to it
	Base base;
	long mon_added;
	
	public OwnableSatelliteDataSaver()
	{
		is_data_saved=false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doLoadData(T sat)
	{
		sat.bldg_in_progress = bldg_in_prog;
		sat.time_finish = t_finish;
		sat.time_start = t_start;
		sat.next_facility_id = next_fac_id;
		sat.last_tax_time = last_tax_t;
		sat.owner = own;
		sat.facilities = (Hashtable<Integer, Facility<?>>) fac.clone(); //unchecked cast warning
		sat.the_base = base;
		sat.time=t;
		sat.tax_money = mon_added;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doSaveData(T sat)
	{
		bldg_in_prog = sat.bldg_in_progress;
		t_finish = sat.time_finish;
		t_start = sat.time_start;
		next_fac_id = sat.next_facility_id;
		last_tax_t = sat.last_tax_time;
		own=sat.owner;
		fac = (Hashtable<Integer, Facility<?>>) sat.facilities.clone(); //unchecked cast warning
		base = sat.the_base;
		t=sat.time;
		mon_added = sat.tax_money;
	}
}
