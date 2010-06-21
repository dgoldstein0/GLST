
public abstract class DataSaver<T extends Saveable> {

	public long t; //time saved
	boolean data_saved;
	
	public DataSaver()
	{
		data_saved=false;
	}
	
	public void saveData(T s){data_saved=true;}
	public abstract void loadData(T s);
	
	public final boolean isDataSaved(){return data_saved;};
}
