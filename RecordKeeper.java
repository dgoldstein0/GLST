import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class exists to keep track of decisions on orders for testing.
 */
public class RecordKeeper {
	
	Map<Order, Order> decided_orders;
	
	public RecordKeeper(List<Order> decisions)
	{
		decided_orders = new HashMap<Order, Order>();
		
		for (Order o : decisions)
			decided_orders.put(o,o);
	}
	
	public void checkDecision(Order o) {
		
		if (decided_orders.containsKey(o))
		{
			if (decided_orders.get(o).getDecision().equals(o.getDecision()))
			{
				//System.out.println("\tDecision verified");
			}
			else
			{
				throw new DecisionCheckException("\tUHOH!  Decision failed to verify!\n"+ o.getClass().getName() + " from player " + o.p_id + ", order_num " + o.order_number + " scheduled at time " + o.scheduled_time); //make some noise
			}
		}
		else
			System.out.println("\tWarning: failed to verify decision - the logs do not tell us whether this order was decided.");
	}
	
	public static class DecisionCheckException extends RuntimeException
	{
		private static final long serialVersionUID = -4385073390106023488L;

		public DecisionCheckException(String s)
		{
			super(s);
		}
	}
}
