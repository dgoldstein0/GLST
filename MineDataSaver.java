
public strictfp class MineDataSaver extends FacilityDataSaver<Mine> {
	
	double m_rate;
	long met_added; //MineDataSaverControl has special support to use this to claw back metal
	
	public MineDataSaver()
	{
		super();
	}

	@Override
	protected void doLoadMoreData(Mine m) {
		
		m.mining_rate = m_rate;
		m.add_met = met_added; //not sure this is necessary
	}

	@Override
	protected void doSaveMoreData(Mine m) {
		
		m_rate = m.mining_rate;
		met_added = m.add_met;
	}
}
