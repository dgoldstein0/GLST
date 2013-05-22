import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public strictfp class DataSaver<T extends Saveable<T>> {

	private long t; //time saved
	private boolean data_saved;
	private HashMap<Field, Object> saved_values;
	private List<Field> fields_to_save;
	
	public DataSaver(List<Field> fields)
	{
		data_saved = false;
		fields_to_save = fields;
		
		saved_values = new HashMap<Field, Object>();
	}
	
	final public void saveData(T s, long time)
	{
		data_saved=true;
		t = time;
		doSaveData(s);
	}
	
	final public void loadData(T s)
	{
		doLoadData(s);
	}
	
	protected void doLoadData(T s) {
		
		for (Field f : saved_values.keySet())
		{
			try {
				
				Object o = saved_values.get(f);
				
				if (o instanceof Cloneable)
				{
					// This line is equivalent to o = o.clone() - however,
					// the trouble is that clone() is protected, so we can't
					// call it directly.  Also casting it to the proper type
					// is out of the question (note clone is NOT a method of
					// Cloneable).
					o = o.getClass().getMethod("clone").invoke(o);
				}
				f.set(s, o);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck IllegalArgumentException");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck IllegalAccessException");
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck SecurityException");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck InvocationTargetException");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck NoSuchMethodException - this REALLY shouldn't be possible");
			}
		}
	}

	protected void doSaveData(T s) {
		for (Field f : fields_to_save)
		{
			try {
				Object o = f.get(s);
				
				if (o instanceof Cloneable)
				{
					// This line is equivalent to o = o.clone() - however,
					// the trouble is that clone() is protected, so we can't
					// call it directly.  Also casting it to the proper type
					// is out of the question (note clone is NOT a method of
					// Cloneable).
					o = o.getClass().getMethod("clone").invoke(o);
				}
				
				saved_values.put(f, o);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck IllegalArgumentException");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck IllegalAccessException");
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck SecurityException");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck InvocationTargetException");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Oh fuck NoSuchMethodException - this REALLY shouldn't be possible");
			}
		}
	}
	
	public final boolean isDataSaved(){return data_saved;};
	public long getTime(){return t;}
}
