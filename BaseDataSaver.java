
public class BaseDataSaver extends FacilityDataSaver<Base> {

	float sldr;
	int max_sldr;
	
	public BaseDataSaver()
	{
		super();
	}

	@Override
	public void loadData(Base b) {
		super.loadData(b);

		b.soldier = sldr;
		b.max_soldier = max_sldr;
	}

	@Override
	public void saveData(Base b) {
		super.saveData(b);
		sldr = b.soldier;
		max_sldr = b.max_soldier;
	}
}
