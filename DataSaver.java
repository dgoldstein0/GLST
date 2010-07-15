
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
		doSaveData(s);
	}
	
	protected abstract void doSaveData(T s);
	public abstract void loadData(T s);
	
	public final boolean isDataSaved(){return data_saved;};
}
