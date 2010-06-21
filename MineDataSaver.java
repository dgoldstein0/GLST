
public class MineDataSaver extends FacilityDataSaver<Mine> {
	
	double m_rate;
	
	public MineDataSaver()
	{
		super();
	}

	@Override
	public void loadData(Mine m) {
		super.loadData(m);
		m.mining_rate = m_rate;
	}

	@Override
	public void saveData(Mine m) {
		super.saveData(m);
		m_rate = m.mining_rate;
	}
}
