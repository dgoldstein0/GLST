import java.util.HashMap;

public strictfp class MissileListDataSaver extends DataSaver<MissileList> {

	HashMap<Missile.MissileId, Missile> tbl;
	
	public MissileListDataSaver() {
		
		super();
	}

	@Override
	protected void doSaveData(MissileList l) {
		
		tbl = (HashMap<Missile.MissileId, Missile>) l.table.clone();

	}

	@Override
	protected void doLoadData(MissileList l) {
		
		l.table = (HashMap<Missile.MissileId, Missile>) tbl.clone();
	}

}
