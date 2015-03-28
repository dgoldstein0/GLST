package galactic_strategy.sync_engine;

import galactic_strategy.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * @author David
 *
 * @param <T> the class that we want to save data from.
 */
public strictfp class DataSaverControl<T extends Saveable<T>> {

	protected int index;
	protected final DataSaver<T>[] saved_data;
	protected final T the_obj;
	
	@SuppressWarnings("unchecked")
	public DataSaverControl(T s)
	{
		index = 0;
		the_obj = s;
		
		saved_data = new DataSaver[Constants.data_capacity];
		List<Field> fields_to_save = getFields(the_obj);
		for(int i=0; i < saved_data.length; ++i)
			saved_data[i] = new DataSaver<T>(fields_to_save);
	}
	
	private List<Field> getFields(T obj)
	{
		Class<?> cls;
		List<Field> fields_to_save = new ArrayList<Field>();
		
		cls = obj.getClass();
		do {
			Field[] fields = cls.getDeclaredFields();
			
			for (int i=0; i < fields.length; i++)
			{
				int modifiers = fields[i].getModifiers();
				if (fields[i].getName() == "data_control" ||
					fields[i].isEnumConstant() ||
					Modifier.isFinal(modifiers) ||
					Modifier.isStatic(modifiers))
					continue;
				
				fields[i].setAccessible(true);
				fields_to_save.add(fields[i]);
			}
			
			cls = cls.getSuperclass();
		} while (cls != null);
		
		return fields_to_save;
	}
	
	//loading and saving data functions.
	final public void saveData(long correct_time)
	{
		//if(the_obj instanceof Ship)
		//	System.out.println(Integer.toString(((Ship)the_obj).id.queue_id) + " saving time " + Long.toString(((Ship)the_obj).time) + " at index " + Integer.toString(index));

		int prev_index = getPreviousIndex(index);
		
		if(saved_data[prev_index].getTime() == correct_time)
		{
			saved_data[prev_index].saveData(the_obj, correct_time);
			if (correct_time != 0)
				throw new RuntimeException();
		}
		else
		{
			saved_data[index].saveData(the_obj, correct_time);
	
			index = getNextIndex(index);
		}
	}
	

	final public void revertToTime(long t) throws DataSaverControl.DataNotYetSavedException
	{		
		int indx = getIndexForTime(t);

		if (saved_data[indx].isDataSaved())
		{	
			saved_data[indx].loadData(the_obj);
			
			index = getNextIndex(indx); //index points to NEXT DataSaver
		}
		else
		{
			throw new DataNotSavedException("Data for object not saved for time " + t);
		}
	}
	
	public int getIndexForTime(long t) throws DataNotYetSavedException
	{
		int stepback = (int) ((saved_data[getPreviousIndex(index)].getTime()-t)/Constants.TIME_GRANULARITY + 1);
		stepback += (t % Constants.TIME_GRANULARITY != 0) ? 1 : 0;
		
		int indx = -1;
		//System.out.println("load data: t is " + Long.toString(t) + " and time is " + Long.toString(time) + ", so step back... " + Integer.toString(stepback));
		if (stepback > Constants.data_capacity)
		{
			//TODO: is this the right way to deal with these errors?
			throw new RuntimeException("Error loading ship data: the delay is too long");
		}
		else if(stepback <= 0)
		{
			throw new DataNotYetSavedException(
				"Major consistency error: stepback in getIndexForTime is " +
				Integer.toString(stepback) + "with t=" + Long.toString(t)
			);
		}
		else
		{
			indx = index - stepback;
			if (stepback > index)
				indx += Constants.data_capacity;			
		}
		
		if (saved_data[indx].getTime() != t)
			throw new RuntimeException(
				"saved_data[" + indx + "].t is " + saved_data[indx].getTime() + " and t is " + t
			);
		
		return indx;
	}
	
	protected int getNextIndex(int i)
	{
		return (i != saved_data.length-1) ? i+1 : 0;
	}
	
	protected int getPreviousIndex(int i)
	{
		return (i != 0)? i-1 : saved_data.length-1;
	}
	
	public static class DataNotYetSavedException extends RuntimeException
	{
		/**
		 * Auto-generated serialVersionUID for Serializable
		 */
		private static final long serialVersionUID = 3998188571908079611L;

		public DataNotYetSavedException(String msg)
		{
			super(msg);
		}
	}
	
	public static class DataNotSavedException extends RuntimeException
	{

		/**
		 * Auto-generated serialVersionUID for Serializable 
		 */
		private static final long serialVersionUID = 2709446774330687410L;
		
		public DataNotSavedException(String s)
		{
			super(s);
		}
	}
	
	public T getThe_obj() {return the_obj;}
}
