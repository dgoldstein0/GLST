import java.util.HashMap;
import java.util.Set;

public class MissileList implements RelaxedSaveable<MissileList> {
	
	HashMap<Missile.MissileId, Missile> table;
	MissileListDataControl data_control;
	long time;
	
	public MissileList()
	{
		table = new HashMap<Missile.MissileId, Missile>();
		data_control = new MissileListDataControl(this);
		time=0;
		data_control.saveData();
	}

	public synchronized Missile get(Missile.MissileId key)
	{
		return table.get(key);
	}
	
	public synchronized Missile put(Missile.MissileId key, Missile m, long t)
	{
		Missile ret = table.put(key, m);
		time = t;
		data_control.saveData();
		
		return ret;
	}
	
	public synchronized Missile remove(Missile.MissileId key, long t)
	{
		time=t;
		Missile m = table.remove(key);
		data_control.saveData();
		
		return m;
	}
	
	public synchronized Set<Missile.MissileId> keySet()
	{
		return table.keySet();
	}
	
	@Override
	public long getTime() {
		
		return time;
	}

	@Override
	public DataSaverControl<MissileList, ? extends DataSaver<MissileList>> getDataControl() {
		
		return data_control;
	}

	@Override
	public void handleDataNotSaved(long time) {
		
		System.out.println("Impossible: MissileList.handleDataNotSaved has been invoked.");
	}
	
	public HashMap<Missile.MissileId, Missile> getTable(){return table;}
	public synchronized void setTable(HashMap<Missile.MissileId, Missile> t){table=t;}
}
