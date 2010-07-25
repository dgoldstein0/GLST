
public abstract class DataSaver<T extends Saveable<T>> {

	public long t; //time saved
	boolean data_saved;
	
	public DataSaver()
	{
		data_saved=false;
	}
	
	final public void saveData(T s)
	{
		data_saved=true;
		t = s.getTime();
		doSaveData(s);
	}
	
	final public void loadData(T s)
	{
		s.setTime(t);
		doLoadData(s);
	}
	
	protected abstract void doSaveData(T s);
	protected abstract void doLoadData(T s);
	
	public final boolean isDataSaved(){return data_saved;};
}
