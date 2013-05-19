import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class exists to aide testing.  It originally only
 * tracked of decisions on orders for testing, but now is also
 * responsible for saving the state of the game at predefined
 * spots.
 */
public class RecordKeeper {
	
	public static enum SAVE_TYPE {BEFORE, AFTER};
	
	private Map<Order, Order> decided_orders;
	private Map<Long, String> results_before;
	private Map<Long, String> results_after;
	private HashSet<Long> save_points;
	private boolean suppress_unchecked_decisions;
	
	public RecordKeeper(List<Order> decisions, List<Long> saves)
	{
		decided_orders = new HashMap<Order, Order>();
		results_before = new HashMap<Long, String>();
		results_after = new HashMap<Long, String>();
		save_points = new HashSet<Long>();
		save_points.addAll(saves);
		
		if (decisions != null)
		{
			suppress_unchecked_decisions = false;
			for (Order o : decisions)
			{
				decided_orders.put(o,o);
			}
		}
		else
			suppress_unchecked_decisions = true;
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
		else if (!suppress_unchecked_decisions)
			System.err.println("\tWarning: failed to verify decision - the logs do not tell us whether this order was decided.");
	}
	
	public static class DecisionCheckException extends RuntimeException
	{
		private static final long serialVersionUID = -4385073390106023488L;

		public DecisionCheckException(String s)
		{
			super(s);
		}
	}

	public void maybeSaveData(GameControl GC, long update_to, SAVE_TYPE marker) {
		
		if (save_points.contains(update_to))
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(os);
			encoder.setExceptionListener(new GameUpdater.MyExceptionListener());
			encoder.writeObject(GC.players);
			encoder.writeObject(GC.map);
			encoder.close();
			String output = "Save " + marker + ": " + update_to +"\n";
			output += os.toString();
			
			if (marker == SAVE_TYPE.BEFORE)
				results_before.put(update_to, output);
			else if (marker == SAVE_TYPE.AFTER)
				results_after.put(update_to, output);
		}
	}
	
	public List<String> getSavedResults()
	{
		List<String> all_results = new ArrayList<String>();
		
		List<Long> key_list = new ArrayList<Long>();
		key_list.addAll(results_before.keySet());
		Collections.sort(key_list); 
		for (Long time : key_list)
		{
			all_results.add(results_before.get(time));
			all_results.add(results_after.get(time));
		}
		
		return all_results;
	}
}
