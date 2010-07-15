import java.util.HashSet;

public abstract class FacilityDataSaver<T extends Facility<T>> extends DataSaver<T> {

	HashSet<Targetter<?>> aggr;
	int endu;
	int dmg;
	long t;
	boolean alive;
	
	public FacilityDataSaver()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadData(T f) {

		f.aggressors = (HashSet<Targetter<?>>) aggr.clone();//unchecked cast warning
		f.endurance = endu;
		f.damage = dmg;
		f.last_time = t;
		f.is_alive = alive;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected void doSaveData(T f) {
	
		aggr = (HashSet<Targetter<?>>) f.aggressors.clone(); //unchecked cast warning
		endu= f.endurance;
		dmg= f.damage;
		t=f.last_time;
		alive=f.is_alive;
	}
	
	protected abstract void doSaveMoreData(T f);

}
