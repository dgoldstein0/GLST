import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;


public class GenericDataSaver<T extends Saveable<T>> extends DataSaver<T> {

	HashMap<Field, Object> saved_values;
	
	GenericDataSaver(T obj)
	{
		Class<?> cls;
		saved_values = new HashMap<Field, Object>();
		
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
				saved_values.put(fields[i], null);
			}
			
			cls = cls.getSuperclass();
		} while (cls != null);
	}
	
	@Override
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

	@Override
	protected void doSaveData(T s) {
		for (Field f : saved_values.keySet())
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

}
