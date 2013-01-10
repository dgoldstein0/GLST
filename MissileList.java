import java.util.Iterator;
import java.util.TreeMap;
import java.util.Set;

public strictfp class MissileList implements Saveable<MissileList> {
	
	TreeMap<Missile.MissileId, Missile> table;
	MissileListDataControl data_control;
	volatile long time;
	
	public MissileList()
	{
		table = new TreeMap<Missile.MissileId, Missile>();
		data_control = new MissileListDataControl(this);
		time=0;
	}

	public synchronized Missile get(Missile.MissileId key)
	{
		return table.get(key);
	}
	
	public synchronized Missile put(Missile.MissileId key, Missile m, long t)
	{
		Missile ret = table.put(key, m);
		time = t;
		
		return ret;
	}
	
	public synchronized Missile remove(Missile.MissileId key, long t)
	{
		time=t;
		Missile m = table.remove(key);
		
		return m;
	}
	
	public synchronized Set<Missile.MissileId> keySet()
	{
		return table.keySet();
	}
	
	@Override
	public long getTime() {return time;}
	
	@Override
	public void setTime(long t) {time=t;}

	@Override
	public DataSaverControl<MissileList, ? extends DataSaver<MissileList>> getDataControl() {
		
		return data_control;
	}

	@Override
	public void handleDataNotSaved(long time) {
		
		System.out.println("Impossible: MissileList.handleDataNotSaved has been invoked.");
	}
	
	public synchronized TreeMap<Missile.MissileId, Missile> getTable(){return table;}
	public synchronized void setTable(TreeMap<Missile.MissileId, Missile> t){table=t;}

	public void update(long time) {
		
		this.time = time;
		synchronized(this)
		{
			Iterator<Missile.MissileId> missile_iteration = keySet().iterator();
			for(Missile.MissileId i; missile_iteration.hasNext();)
			{
				i=missile_iteration.next();
				get(i).update(time, missile_iteration); //returns true if the missile detonates
			}
		}
	}
}
