import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**GameSimulator
 * This class is meant to simulate games, for testing purposes.  It will be tightly
 * integrated with both GameUpdater and GameControl.
 * */
public class GameSimulator {
	
	public static void main(String[] args)
	{
		{
			//test case 1: time-independence of the game
			System.out.println("Running Test 1...");
			
			long[] random_times = {39l,198l,235l,240l,400l,405l,509l,737l,801l,840l,874l,940l,1000l};
			List<SimulateAction> actions1 = new ArrayList<SimulateAction>();
			for(long num : random_times)
				actions1.add(new SimulateAction(num,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(1000l,SimulateAction.ACTION_TYPE.SAVE));
			Simulation sim1 = new Simulation("simplemap.xml",1,actions1);
			
			List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
			actions2.add(new SimulateAction(1000l,SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(1000l,SimulateAction.ACTION_TYPE.SAVE));
			Simulation sim2 = new Simulation("simplemap.xml",1,actions2);
			compareResults(1, sim1.simulate(null), sim2.simulate(null));
		}
		
		{
			System.out.println("Running Test 2...");
			//test case 2: verifying my orders-left-in-queue theory
			
			FacilityBuildOrder shipyard_build_order = new FacilityBuildOrder();
			shipyard_build_order.setBldg_type(FacilityType.SHIPYARD);
			shipyard_build_order.setP_id(0);
			shipyard_build_order.setOrder_number(1);
			SatelliteDescriber<Planet> eulenspiegel_desc = new SatelliteDescriber<Planet>();
				GSystemDescriber azha_sys = new GSystemDescriber();
					azha_sys.setId(1);
				eulenspiegel_desc.setBoss_describer(azha_sys);
				eulenspiegel_desc.setId(0);
			shipyard_build_order.setSat_desc(eulenspiegel_desc);
			
			shipyard_build_order.setScheduled_time(73l); //note the time here
			
			List<SimulateAction> actions1 = new ArrayList<SimulateAction>();
			actions1.add(new SimulateAction(0l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions1.add(new SimulateAction(75l,SimulateAction.ACTION_TYPE.UPDATE));
			
			Simulation sim1 = new Simulation("simplemap.xml", 1, actions1);
			sim1.simulate(null);
		}
		
		{
			System.out.println("Running Test 3...");
			//test time-independence with FacilityBuildOrder
			
			FacilityBuildOrder shipyard_build_order = new FacilityBuildOrder();
			shipyard_build_order.setBldg_type(FacilityType.SHIPYARD);
			shipyard_build_order.setP_id(0);
			shipyard_build_order.setOrder_number(1);
			SatelliteDescriber<Planet> eulenspiegel_desc = new SatelliteDescriber<Planet>();
				GSystemDescriber azha_sys = new GSystemDescriber();
					azha_sys.setId(1);
				eulenspiegel_desc.setBoss_describer(azha_sys);
				eulenspiegel_desc.setId(0);
			shipyard_build_order.setSat_desc(eulenspiegel_desc);
			
			shipyard_build_order.setScheduled_time(73l); //note the time here
			
			
			List<SimulateAction> actions1 = new ArrayList<SimulateAction>();
			actions1.add(new SimulateAction(0l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions1.add(new SimulateAction(85l,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.SAVE));
			long[] random_times = {198l,235l,240l,400l,405l,509l,737l,801l,840l,874l,940l,1000l};
			for(long num : random_times)
				actions1.add(new SimulateAction(num,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim1 = new Simulation("simplemap.xml", 1, actions1);
			List<String> results1 = sim1.simulate(null);
			
			
			
			List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
			actions2.add(new SimulateAction(85l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(86l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
			List<String> results2 = sim2.simulate(null);
			
			compareResults(3, results1, results2);
			//saveResultsToFile(results1, "test3p1.txt");
			//saveResultsToFile(results2, "test3p2.txt");
		}
		
		{
			//Test case 4
			System.out.println("Running Test 4...");
			//test time-independence with FacilityBuildOrder
			for(FacilityType t : FacilityType.values())
			{
				FacilityBuildOrder build_order = new FacilityBuildOrder();
				build_order.setBldg_type(t);
				build_order.setP_id(0);
				build_order.setOrder_number(1);
				SatelliteDescriber<Planet> eulenspiegel_desc = new SatelliteDescriber<Planet>();
					GSystemDescriber azha_sys = new GSystemDescriber();
						azha_sys.setId(1);
					eulenspiegel_desc.setBoss_describer(azha_sys);
					eulenspiegel_desc.setId(0);
				build_order.setSat_desc(eulenspiegel_desc);
				
				build_order.setScheduled_time(73l); //note the time here
				
				
				List<SimulateAction> actions1 = new ArrayList<SimulateAction>();
				actions1.add(new SimulateAction(0l, build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
				actions1.add(new SimulateAction(80l,SimulateAction.ACTION_TYPE.UPDATE));
				actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.UPDATE));
				actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.SAVE));
				long[] random_times = {198l,235l,240l,400l,405l,509l,737l,801l,840l,874l,940l,50000l};
				for(long num : random_times)
					actions1.add(new SimulateAction(num,SimulateAction.ACTION_TYPE.UPDATE));
				actions1.add(new SimulateAction(50000l, SimulateAction.ACTION_TYPE.SAVE));
				
				Simulation sim1 = new Simulation("simplemap.xml", 1, actions1);
				List<String> results1 = sim1.simulate("log1.txt");
				
				
				
				List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
				actions2.add(new SimulateAction(80l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(85l, build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
				actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
				actions2.add(new SimulateAction(50000l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(50000l, SimulateAction.ACTION_TYPE.SAVE));
				
				Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
				List<String> results2 = sim2.simulate("log2.txt");
				
				
				//saveResultsToFile(results2, "results2.txt");
				compareResults(4, results1, results2);
			}
		}
		
		//NOTE: neither test 5 nor test 6 have order_numbers for their orders.
		//They were made before order numbers were made :(
		
		{
			//Test Case 5
			System.out.println("Running Test 5...");
			try{
				
				System.out.println("\tRunning part 1...");
				
				Simulation sim1 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayerbuildafewthings.txt")
						),
						false, 10, null
					);
				List<String> l1 = sim1.simulate("test5-gold.txt");
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayerbuildafewthings.txt")
						),
						true, 10, null
					);
				List<String> l2 = sim2.simulate("test5-modified.txt");
				
				compareResults(5, l1, l2);
				
			} catch(FileNotFoundException fnfe){System.out.println("FileNotFound for Test 5");}
		}
		
		{
			//Test Case 6
			System.out.println("Running Test 6...");
			try{
				
				System.out.println("\tRunning part 1...");
				
				Simulation sim1 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayercrash.txt")
						),
						false, 3, null
					);
				List<String> l1 = sim1.simulate("test6-gold.txt");
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayercrash.txt")
						),
						true, 3, null
					);
				List<String> l2 = sim2.simulate("test6-modified.txt");
				
				compareResults(6, l1, l2);
			} catch(FileNotFoundException fnfe){System.out.println("FileNotFound for Test 6");}
		}
		
		{
			//Test Case 7
			System.out.println("Running Test 7...");
			try{
				
				System.out.println("\tRunning part 1...");
				
				Simulation sim1 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/test7-singleplayerattackbug.txt")
						),
						false, 3, null
					);
				List<String> l1 = sim1.simulate("test7-gold.txt");
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/test7-singleplayerattackbug.txt")
						),
						true, 3, null
					);
				List<String> l2 = sim2.simulate("test7-modified.txt");
				
				compareResults(7, l1, l2);
			} catch(FileNotFoundException fnfe){System.out.println("FileNotFound for Test 7");}
		}
		
		{
			//Test Case 8 = first multiplayer test
			System.out.println("Running Test 8...");
			try{
				//actual end time is 281180
				
				Simulation sim1 = loadSimFromFile("simplemap.xml", 2,
						new FileInputStream(
							new File("testcases/test8-host.txt")
						),
						false, 600, 120000l
					);

				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 2,
						new FileInputStream(
							new File("testcases/test8-guest.txt")
						),
						false, 600, 120000l
					);
				
				System.out.println("Test 8 has same orders: " + hasSameOrders(sim1, sim2));
				//correctOrders(sim1, sim2, 281180l);
				
				System.out.println("\tRunning part 1...");
				List<String> l1 = sim1.simulate("test8-a.txt");
				
				System.out.println("\tRunning part 2...");
				List<String> l2 = sim2.simulate("test8-b.txt");
				
				compareResults(8, l1, l2);
				
				saveResultsToFile(l1, "test8p1.txt");
				saveResultsToFile(l2, "test8p2.txt");
			} catch(FileNotFoundException fnfe){System.out.println("FileNotFound for Test 8");}
		}
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
				boolean match = l1.get(i).equals(l2.get(i));
				if(!match)
					System.out.println("\tSave point " + i + " does not match: " + l1.get(i).substring(0, l1.get(i).indexOf('\n')));
				else
					System.out.println("\tSave point " + i + " matches: " + l2.get(i).substring(0, l2.get(i).indexOf('\n')));
				identical = identical && match;
			}
			
			if(identical)
				System.out.println("\ttest PASSED");
			else
				System.out.println("\ttest FAILED");
		}
	}
	
	public static boolean hasSameOrders(Simulation sim1, Simulation sim2)
	{
		Set<Order> o1 = new HashSet<Order>();
		Set<Order> o2 = new HashSet<Order>();
		
		for (SimulateAction action : sim1.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER)
			{
				boolean retval = o1.add(action.the_order);
				if (!retval)
					throw new RuntimeException("sim1 has a duplicate order!");
			}
		}
		
		for (SimulateAction action : sim2.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER)
			{
				boolean retval = o2.add(action.the_order);
				if (!retval)
					throw new RuntimeException("sim2 has a duplicate order!");
			}
		}
		
		for (Order o : o1)
		{
			if (!o2.contains(o))
				System.out.println("\tsim2 is missing " + o.getClass().getName() + " from player " + o.p_id + " order_number " + o.order_number + " at time " + o.scheduled_time);
		}
		
		for (Order o : o2)
		{
			if (!o1.contains(o))
				System.out.println("\tsim1 is missing " + o.getClass().getName() + " from player " + o.p_id + " order_number " + o.order_number + " at time " + o.scheduled_time);
		}
		
		return o1.containsAll(o2) && o2.containsAll(o1);
	}
	
	/**
	 * If this function has to do anything, it will stick extra orders at end_time.
	 * This almost always means trouble.
	 * 
	 * @param sim1 the first simulation (game from p1's perspective)
	 * @param sim2 the second simulation (game from p2's perspective)
	 * @param end_time the end time of the game
	 */
	public static void correctOrders(Simulation sim1, Simulation sim2, long end_time)
	{
		Set<Order> o1 = new HashSet<Order>();
		Set<Order> o2 = new HashSet<Order>();
		
		for (SimulateAction action : sim1.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER)
			{
				o1.add(action.the_order);
			}
		}
		
		for (SimulateAction action : sim2.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER)
			{
				o2.add(action.the_order);
			}
		}
		
		for (Order o : o1)
		{
			if (!o2.contains(o))
				sim2.actions.add(
					new SimulateAction(
							end_time,
							o,
							SimulateAction.ACTION_TYPE.SCHEDULE_ORDER,
							SimulateAction.ORDER_TYPE.NONE_SPECIFIED
						)
					);
		}
		
		for (Order o : o2)
		{
			if (!o1.contains(o))
				sim1.actions.add(
						new SimulateAction(
								end_time,
								o,
								SimulateAction.ACTION_TYPE.SCHEDULE_ORDER,
								SimulateAction.ORDER_TYPE.NONE_SPECIFIED
							)
						);
		}
		
		sim1.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.UPDATE));
		sim1.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.SAVE));
		sim2.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.UPDATE));
		sim2.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.SAVE));
	}
	
	public static void saveResultsToFile(List<String> results, String filename)
	{
		try {
			PrintWriter writer = new PrintWriter(filename);
			for(String s : results)
			{
				writer.print(s);
				writer.println();
			}
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**loadSimFromFile
	 * 
	 * @param map the path to the map file.
	 * @param num_players the number of players in the simulated game.
	 * @param remove_updates false to use the updates found in the file,
	 * 		true to throw them away and only use extra updates.
	 * @param num_updates_and_saves the number of updates/saves in the simulation,
	 * 		occurring at increments of the end_time divided by num_updates_and_saves.
	 * @param end_time the time at which the simulation should end.  If this is null,
	 * 		the simulation ends right after the last entry in the given file.
	 * 
	 * @return a Simulation with the desired sequence of events.
	 */
	public static Simulation loadSimFromFile(String map,
											 int num_players,
											 InputStream file,
											 boolean remove_updates,
											 int num_updates_and_saves,
											 Long end_time)
	{
		List<SimulateAction> actions = new ArrayList<SimulateAction>();
		XMLDecoder d = new XMLDecoder(file);
		d.setExceptionListener(new XMLErrorDetector());
		try{
			while(true)
			{
				SimulateAction action = (SimulateAction)d.readObject();
				actions.add(action);
			}
		}
		catch(ArrayIndexOutOfBoundsException e){}
		d.close();
		
		ArrayList<SimulateAction> new_actions = new ArrayList<SimulateAction>();
		SimulateAction last = null;

		//Find last update
		for (int i=0; i < actions.size(); i++)
		{
			SimulateAction action = actions.get(i);
			
			//destroy anything happening after end_time.
			if (end_time != null && action.do_at_time > end_time)
			{
				actions.remove(i);
				i--;
				continue;
			}
			
			if (action.type.equals(SimulateAction.ACTION_TYPE.UPDATE))
			{
				last = action;
			}
			else
				new_actions.add(action);
		}
			
		if (remove_updates)
		{
			actions = new_actions;
		}
		
		if (end_time == null)
			end_time = last.do_at_time;
		
		for(int i=1; i <= num_updates_and_saves; i++)
		{
			SimulateAction update = new SimulateAction(end_time*i/num_updates_and_saves,
													   SimulateAction.ACTION_TYPE.UPDATE);
			actions.add(update);
			actions.add(new SimulateAction(end_time*i/num_updates_and_saves,
										   SimulateAction.ACTION_TYPE.SAVE));
		}
		Collections.sort(actions, new SimulateAction.Comparer());
		
		return new Simulation(map, num_players, actions);
	}
	
	public static class Simulation
	{
		final int num_players;
		List<SimulateAction> actions;
		String map_location;
		
		public Simulation(String map_location, int num_players, List<SimulateAction> actions)
		{
			this.num_players = num_players;
			this.actions = actions;
			this.map_location = map_location;
		}
				
		List<String> simulate(String logfile_name)
		{
			GameControl GC = new GameControl(null);
			GameInterface.GC = GC;
			GC.startTest(num_players, true, new File(map_location));
			GC.updater.setupLogFile((logfile_name == null) ? "log.txt" : logfile_name);
			
			ArrayList<String> results = new ArrayList<String>();
			
			for(SimulateAction action : actions)
			{
				((SimulatedTimeControl)GC.updater.TC).advanceTime(action.do_at_time);
				switch(action.type)
				{
					case UPDATE:
						try {
							GC.updater.updateGame();
						} catch (DataSaverControl.DataNotYetSavedException e) {
							e.printStackTrace();
						}
						break;
					case SCHEDULE_ORDER:
						GC.updater.scheduleOrder(action.the_order);
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
						break;
					case RECEIVED_DECISION:
						GC.updater.decideOrder(new Message(Message.Type.DECISION, action.the_order));
						break;
				}
			}
			GC.updater.logFile.close();
			return results;
		}
	}
	
	public static class SimulateAction
	{
		public static enum ACTION_TYPE{UPDATE,SCHEDULE_ORDER,SAVE, RECEIVED_DECISION;}
		public static enum ORDER_TYPE{NONE_SPECIFIED,LOCAL,REMOTE;}
		
		long do_at_time;
		Order the_order;
		ACTION_TYPE type;
		ORDER_TYPE order_type;
		
		public SimulateAction(long time, Order o, ACTION_TYPE t, ORDER_TYPE ot)
		{
			do_at_time = time;
			the_order = o;
			type=t;
			order_type=ot;
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
			return cur_time;
		}
		
		public void advanceTime(long t)
		{
			if (t < cur_time)
				throw new RuntimeException("Monotonicity Error");
			cur_time = t;
		}
	}
}
