import java.util.Set;
import java.util.HashSet;

public class ReversionEffects {

	Set<Order> orders_to_redo;
	Set<RevertObj> objects_to_revert;
	
	public ReversionEffects(Set<Order> orders, Set<RevertObj> objs)
	{
		orders_to_redo=orders;
		objects_to_revert = objs;
	}
	
	public ReversionEffects()
	{
		orders_to_redo = new HashSet<Order>();
		objects_to_revert = new HashSet<RevertObj>();
	}
	
	public static class RevertObj
	{
		long time_to_revert;
		Saveable<?> obj;
		
		public RevertObj(Saveable<?> o, long t)
		{
			obj=o;
			time_to_revert=t;
		}
		
		//equals ignores time_to_revert: 2 RevertObj's are equal if their obj's are equal
		public boolean equals(RevertObj o)
		{
			if(o != null)
			{
				if(obj != null)
					return obj.equals(o.obj);
				else
					return (o==null);
			}
			else
				return false;
		}
		
		//overridden to make sure that if two objects are the same according to equals, they have the same hashCode
		public int hashCode()
		{
			if(obj != null)
				return obj.hashCode();
			else
				return 0;
		}
	}
}
