
/**
 * saved_data array is instantiated via the Creator object passed into the constructor
 * @author David
 *
 * @param <T> the inheriting class
 * @param <S> the DataSaver type for the inheriting class
 */
public strictfp abstract class DataSaverControl<T extends Saveable<T>, S extends DataSaver<T> > {

	int index;
	final S[] saved_data;
	final T the_obj;
	
	public DataSaverControl(T s, Creator<T, S> c)
	{
		index=0;
		the_obj=s;
		
		saved_data = c.createArray();
		for(int i=0; i < saved_data.length; ++i)
			saved_data[i] = c.create();
	}
	
	//loading and saving data functions.
	final public void saveData()
	{
		//if(the_obj instanceof Ship)
		//	System.out.println(Integer.toString(((Ship)the_obj).id.queue_id) + " saving time " + Long.toString(((Ship)the_obj).time) + " at index " + Integer.toString(index));

		int prev_index = getPreviousIndex(index);
		if(saved_data[prev_index].t == ((T)the_obj).getTime())
			saved_data[prev_index].saveData(the_obj);
		else
		{
			saved_data[index].saveData(the_obj);
	
			index = getNextIndex(index);
		}
	}
	

	final public void revertToTime(long t) throws DataSaverControl.DataNotYetSavedException
	{		
		if(saved_data[getPreviousIndex(index)].isDataSaved() && saved_data[getPreviousIndex(index)].t > t) //this check helps ensure we do not get into an infinite recursion.
		{
			int indx=getIndexForTime(t);
			//System.out.println(the_obj.getClass().toString() + " revert to time=" + Long.toString(t) + " index=" + Integer.toString(indx));
			if (saved_data[indx].isDataSaved())
			{	
				saved_data[indx].loadData(the_obj);
				
				index = getNextIndex(indx); //index points to NEXT DataSaver
			}
			else
			{
				//the object did not exist at the indicated time.  Delete it...
				//(consider possibly save it for re-creation?)
				
				//remove the object from the data structure
				the_obj.handleDataNotSaved(t);
			}
		}
	}
	
	public abstract int getIndexForTime(long t) throws DataNotYetSavedException;
	
	protected int getNextIndex(int i)
	{
		return (i != saved_data.length-1) ? i+1 : 0;
	}
	
	protected int getPreviousIndex(int i)
	{
		return (i != 0)? i-1 : saved_data.length-1;
	}
	
	//class to create data savers
	public static abstract class Creator<T extends Saveable<T>, S extends DataSaver<T>>
	{
		public abstract S create();
		public abstract S[] createArray();
	}
	
	public static class DataNotYetSavedException extends Exception
	{
		/**
		 * Auto-generated serialVersionUID for Serializable
		 */
		private static final long serialVersionUID = 3998188571908079611L;
		
		final int stepback;
		
		public DataNotYetSavedException(int step)
		{
			stepback = step;
		}
	}
}
