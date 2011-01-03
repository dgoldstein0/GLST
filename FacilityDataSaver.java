import java.util.HashSet;

public strictfp abstract class FacilityDataSaver<T extends Facility<T>> extends DataSaver<T> {

	HashSet<Targetter<?>> aggr;
	int endu;
	int dmg;
	boolean alive;
	
	public FacilityDataSaver()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected void doLoadData(T f) {

		doLoadMoreData(f);
		f.aggressors = (HashSet<Targetter<?>>) aggr.clone();//unchecked cast warning
		f.endurance = endu;
		f.damage = dmg;
		f.is_alive = alive;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected void doSaveData(T f) {
	
		doSaveMoreData(f);
		aggr = (HashSet<Targetter<?>>) f.aggressors.clone(); //unchecked cast warning
		endu= f.endurance;
		dmg= f.damage;
		alive=f.is_alive;
	}
	
	protected abstract void doSaveMoreData(T f);
	protected abstract void doLoadMoreData(T f);
}
