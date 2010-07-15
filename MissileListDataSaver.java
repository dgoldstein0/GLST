import java.util.HashMap;

public class MissileListDataSaver extends DataSaver<MissileList> {

	HashMap<Missile.MissileId, Missile> tbl;
	
	public MissileListDataSaver() {
		
		super();
	}

	@Override
	protected void doSaveData(MissileList l) {
		
		t = l.time;
		tbl = (HashMap<Missile.MissileId, Missile>) l.table.clone();

	}

	@Override
	public void loadData(MissileList l) {
		
		l.time = t;
		l.table = (HashMap<Missile.MissileId, Missile>) tbl.clone();
	}

}
