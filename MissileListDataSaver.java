import java.util.TreeMap;

public strictfp class MissileListDataSaver extends DataSaver<MissileList> {

	TreeMap<Missile.MissileId, Missile> tbl;
	
	public MissileListDataSaver() {
		
		super();
	}

	@Override
	protected void doSaveData(MissileList l) {
		
		synchronized(l)
		{
			tbl = (TreeMap<Missile.MissileId, Missile>) l.table.clone();
		}
	}

	@Override
	protected void doLoadData(MissileList l) {
		
		synchronized(l)
		{
			l.table = (TreeMap<Missile.MissileId, Missile>) tbl.clone();
		}
	}

}
