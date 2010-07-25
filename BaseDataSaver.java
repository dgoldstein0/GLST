import java.util.HashSet;

public class BaseDataSaver extends FacilityDataSaver<Base> {

	float sldr;
	int max_sldr;
	HashSet<Saveable<?>> taker;
	
	public BaseDataSaver()
	{
		super();
	}

	@Override
	protected void doLoadMoreData(Base b) {

		b.soldier = sldr;
		b.max_soldier = max_sldr;
		b.soldier_taker = (HashSet<Saveable<?>>)taker.clone();
	}

	@Override
	protected void doSaveMoreData(Base b) {
		
		sldr = b.soldier;
		max_sldr = b.max_soldier;
		taker = (HashSet<Saveable<?>>)b.soldier_taker.clone();
	}
}
