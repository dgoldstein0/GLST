
public strictfp class MineDataSaver extends FacilityDataSaver<Mine> {
	
	long last_r_time;
	
	public MineDataSaver()
	{
		super();
	}

	@Override
	protected void doLoadMoreData(Mine m) {
		m.last_resource_time = last_r_time;
	}

	@Override
	protected void doSaveMoreData(Mine m) {
		last_r_time = m.last_resource_time;
	}
}
