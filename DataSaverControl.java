import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

//subclasses are responsible for instantiating saved_data array

public abstract class DataSaverControl<T extends Saveable<T>, S extends DataSaver<T> > {

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
	
	//loading and saving data functions.  This is overridden in RelaxedDataSaverControl.
	public void saveData()
	{
		//if(the_obj instanceof Ship)
		//	System.out.println(Integer.toString(((Ship)the_obj).id.queue_id) + " saving time " + Long.toString(((Ship)the_obj).time) + " at index " + Integer.toString(index));
		
		saved_data[index].saveData(the_obj);

		index = getNextIndex(index);
	}
	

	final public Set<Order> revertToTime(long t)
	{		
		if(saved_data[getPreviousIndex(index)].isDataSaved() && saved_data[getPreviousIndex(index)].t > t) //this check helps ensure we do not get into an infinite recursion.  Empty sets from deduceEffectsAfterIndex could serve same purpose
		{
			int indx=getIndexForTime(t);
			//System.out.println(the_obj.getClass().toString() + " revert to time=" + Long.toString(t) + " index=" + Integer.toString(indx));
			if (saved_data[indx].isDataSaved())
			{
				ReversionEffects reversion_effects = deduceEffectedAfterIndex(indx);
				
				doReversionPrep(indx);
				saved_data[indx].loadData(the_obj);
				index = getNextIndex(indx); //index points to NEXT DataSaver
				
				//must loadData before recursion, else risk of recursion trying to revert something that is already being reverted
				Set<Order> orders = reversion_effects.orders_to_redo;
				
				for(Iterator<ReversionEffects.RevertObj> objs_it = reversion_effects.objects_to_revert.iterator(); objs_it.hasNext();)
				{
					ReversionEffects.RevertObj revertable = objs_it.next();					
					
					orders.addAll(revertable.obj.getDataControl().revertToTime(revertable.time_to_revert));
				}
				
				return orders;
			}
			else
			{
				//the object did not exist at the indicated time.  Delete it... or possibly save it for re-creation?
				//remove the object from the data structure
				
				the_obj.handleDataNotSaved(t);
				return new HashSet<Order>();
			}
		}
		else
			return new HashSet<Order>();
	}
	
	public abstract int getIndexForTime(long t);
	protected abstract ReversionEffects deduceEffectedAfterIndex(int i);
	
	protected int getNextIndex(int i)
	{
		return (i != saved_data.length-1) ? i+1 : 0;
	}
	
	protected int getPreviousIndex(int i)
	{
		return (i != 0)? i-1 : saved_data.length-1;
	}
	
	protected void doReversionPrep(int indx){}
	
	//class to create data savers
	public static abstract class Creator<T extends Saveable<T>, S extends DataSaver<T>>
	{
		public abstract S create();
		public abstract S[] createArray();
	}
}
