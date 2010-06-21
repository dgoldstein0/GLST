//subclasses are responsible for instantiating saved_data array
public abstract class DataSaverControl<T extends Saveable> {

	int index;
	DataSaver<T>[] saved_data;
	final T the_obj;
	
	public DataSaverControl(T s)
	{
		index=0;
		the_obj=s;
	}
	
	//loading and saving data functions
	public void saveData()
	{
		//if(this instanceof Ship)
		//	System.out.println(Integer.toString(((Ship)this).id) + " saving time " + Long.toString(time) + " at index " + Integer.toString(index));
		
		saved_data[index].saveData(the_obj);

		index++;
		if (index>GalacticStrategyConstants.data_capacity-1)
			index=0;
	}
	

	public void revertToTime(long t)
	{
		index=getIndexForTime(t);
		//System.out.println(Integer.toString(index)+" "+Integer.toString(stepback));
		if (saved_data[index].isDataSaved())
		{
			saved_data[index].loadData(the_obj);
		}
		else
		{
			//the object did not exist at the indicated time.  Delete it... or possibly save it for re-creation?
			//remove the object from the data structure
			
			the_obj.handleDataNotSaved();
		}
	}
	
	public abstract int getIndexForTime(long t);
}
