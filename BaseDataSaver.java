public strictfp class BaseDataSaver extends FacilityDataSaver<Base> {

	float sldr;
	int max_sldr;
	
	public BaseDataSaver()
	{
		super();
	}

	@Override
	protected void doLoadMoreData(Base b) {

		b.soldier = sldr;
		b.max_soldier = max_sldr;
	}

	@Override
	protected void doSaveMoreData(Base b) {
		
		sldr = b.soldier;
		max_sldr = b.max_soldier;
	}
}
