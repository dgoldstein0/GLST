import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**GameSimulator
 * This class is meant to simulate games, for testing purposes.  It will be tightly
 * integrated with both GameUpdater and GameControl.
 * */
public class GameSimulator {
	
	public static void main(String[] args)
	{
		//test case 1: time-independence of the game
		long[] random_times = {39l,198l,235l,240l,400l,405l,509l,737l,801l,840l,874l,940l,1000l};
		List<Long> update_times = new ArrayList<Long>();
		for(long num : random_times)
			update_times.add(num);
		List<Long> save_times = new ArrayList<Long>();
		save_times.add(1000l);
		Simulation sim1 = new Simulation("simplemap.xml",1,new ArrayList<OrderSpec>(),update_times,save_times);
		Simulation sim2 = new Simulation("simplemap.xml",1,new ArrayList<OrderSpec>(),save_times,save_times);
		compareResults(1, sim1.simulate(), sim2.simulate());
	}
	
	public static void compareResults(int test_num, List<String> l1, List<String> l2)
	{
		System.out.println("Comparing for test "+test_num);
		
		boolean identical = true;
		if(l1.size() != l2.size())
		{
			System.out.println("\tunequal result lengths in test " + test_num);
			System.out.println("\tList 1: " + l1.size() +"; List 2: " + l2.size());
			return;
		}
		else
		{
			for(int i=0; i < l1.size(); ++i)
			{
				identical = identical && l1.get(i).equals(l2.get(i));
			}
			
			if(identical)
				System.out.println("\ttest PASSED");
			else
				System.out.println("\ttest FAILED");
		}
	}

	
	public static class Simulation
	{
		final int num_players;
		PriorityQueue<OrderSpec> orders;
		List<Long> update_times;
		List<Long> save_times;
		String map_location;
		
		public Simulation(String map_location, int num_players, List<OrderSpec> orders, List<Long> update_times, List<Long> save_times)
		{
			this.num_players = num_players;
			this.orders = new PriorityQueue<OrderSpec>(32, new OrderSpec.Comparer());
			this.orders.addAll(orders);
			
			this.update_times = update_times;
			this.save_times = save_times;
			this.map_location = map_location;
		}
		
		public enum TASK{UPDATE,ISSUE_ORDER,SAVE,NONE;}
		
		List<String> simulate()
		{
			GameControl GC = new GameControl(null);
			GC.startTest(num_players, true, new File(map_location));
			
			int next_time_index = 0;
			int next_save_index = 0;
			OrderSpec next_order = null;
			if(!orders.isEmpty())
				next_order = orders.remove();
			
			ArrayList<String> results = new ArrayList<String>();
			
			boolean do_simulate =true;
			
			while(do_simulate)
			{
				//decide next task
				TASK next_task= TASK.NONE;
				long next_task_time = Long.MAX_VALUE;
				
				if(next_time_index < update_times.size() && update_times.get(next_time_index) < next_task_time)
				{
					next_task = TASK.UPDATE;
					next_task_time = update_times.get(next_time_index);
				}
				
				if(next_order != null && next_order.send_at_time < next_task_time)
				{
					next_task = TASK.ISSUE_ORDER;
					next_task_time = next_order.send_at_time;
				}
				
				if(next_save_index < save_times.size() && save_times.get(next_save_index) < next_task_time)
				{
					next_task = TASK.SAVE;
					next_task_time = save_times.get(next_save_index);
				}
				
				switch(next_task)
				{
					case NONE:
						do_simulate=false;
						break;
					case UPDATE:
						((SimulatedTimeControl)GC.updater.TC).advanceTime(next_task_time);
						try {
							GC.updater.updateGame();
						} catch (DataSaverControl.DataNotYetSavedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						++next_time_index;
						break;
					case ISSUE_ORDER:
						((SimulatedTimeControl)GC.updater.TC).advanceTime(next_task_time);
						GC.updater.pending_execution.add(next_order.the_order);
						if(!orders.isEmpty())
							next_order = orders.remove();
						else
							next_order = null;
						break;
					case SAVE:
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						XMLEncoder encoder = new XMLEncoder(os);
						encoder.setExceptionListener(new GameUpdater.MyExceptionListener());
						encoder.writeObject(GC.players);
						encoder.writeObject(GC.map);
						encoder.close();
						String output = "Save @" + GC.updater.getTime() +"\n";
						output += os.toString();
						results.add(output);
						++next_save_index;
						break;
				}
			}
			return results;
		}
	}
	
	public static class OrderSpec
	{
		final long send_at_time;
		final Order the_order;
		
		public OrderSpec(long time, Order o)
		{
			send_at_time = time;
			the_order = o;
		}
		
		public static class Comparer implements Comparator<OrderSpec>, Serializable
		{
			private static final long serialVersionUID = 6664455814705885702L;

			@Override
			/**Compares OrderSpec's by comparing send_at_times*/
			public int compare(OrderSpec o1, OrderSpec o2) {
				
				if(o2 == null || o1==null)
				{
					throw new IllegalArgumentException();
				}
				else
				{
					if(o2.send_at_time > o1.send_at_time)
						return -1; //object is less than o
					else if(o2.send_at_time == o1.send_at_time)
						return 0; //object "equals" o
					else
						return 1; //object greater than o
				}
			}
		}
	}
	
	public static class SimulatedTimeControl implements TimeManager
	{
		/**in milliseconds*/
		long cur_time; 
		
		public SimulatedTimeControl()
		{
			cur_time = 0;
		}
		
		@Override
		public long getNanoTime() {
			return 1000000*getTime();
		}

		@Override
		public long getNextTimeGrain() {
			return TimeControl.getTimeGrainAfter(getTime());
		}

		@Override
		public long getTime() {
			// TODO Auto-generated method stub
			return cur_time;
		}
		
		public void advanceTime(long t)
		{
			cur_time = t;
		}
	}
}
