import java.io.Serializable;
import java.util.Comparator;

public class SimulateAction
{
	public static enum ACTION_TYPE{UPDATE,SCHEDULE_ORDER,RECEIVED_DECISION;}
	public static enum ORDER_TYPE{NONE_SPECIFIED,LOCAL,REMOTE;}
	
	long do_at_time;
	Order the_order;
	ACTION_TYPE type;
	ORDER_TYPE order_type;
	public int number; // for total ordering - do_at_times only give a partial ordering, since we haven't specified the order of multiple things that are at the same do_at_time
	
	public SimulateAction(long time, Order o, ACTION_TYPE t, ORDER_TYPE ot)
	{
		do_at_time = time;
		the_order = o;
		type=t;
		order_type=ot;
		number = -1;
	}
	
	@Deprecated
	public SimulateAction(long time, Order o, ACTION_TYPE t)
	{
		do_at_time = time;
		the_order = o;
		type=t;
		order_type=ORDER_TYPE.NONE_SPECIFIED;
	}
	
	public SimulateAction(long time, ACTION_TYPE t)
	{
		do_at_time = time;
		type = t;
		if(type == ACTION_TYPE.SCHEDULE_ORDER)
			throw new IllegalArgumentException();
		the_order = null;
		order_type = ORDER_TYPE.NONE_SPECIFIED;
	}
	
	public static class Comparer implements Comparator<SimulateAction>, Serializable
	{
		private static final long serialVersionUID = 6664455814705885702L;

		@Override
		/**Compares OrderSpec's by comparing send_at_times*/
		public int compare(SimulateAction a1, SimulateAction a2) {
			
			if(a2 == null || a1==null)
			{
				throw new IllegalArgumentException();
			}
			else
			{
				if(a2.do_at_time > a1.do_at_time)
					return -1; //object is less than o
				else if(a2.do_at_time == a1.do_at_time)
					return 0; //object "equals" o
				else
					return 1; //object greater than o
			}
		}
	}
	
	@Deprecated
	public SimulateAction()
	{
		order_type=ORDER_TYPE.NONE_SPECIFIED;
	}
	
	public long getDo_at_time(){return do_at_time;}
	public ORDER_TYPE getOrder_type(){return order_type;}
	public void setOrder_type(ORDER_TYPE ot){order_type=ot;}
	public void setDo_at_time(long t){do_at_time=t;}
	public Order getThe_order(){return the_order;}
	public void setThe_order(Order o){the_order = o;}
	public ACTION_TYPE getType(){return type;}
	public void setType(ACTION_TYPE t){type=t;}
}