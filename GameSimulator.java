import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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
		// This is only necessary when we call loadSimFromFile before simulate (simulate calls preload by creating a GameControl)
		try {
			Resources.preload();
		} catch (IOException e) {
			System.err.println("Problem preloading resources.  Exiting.");
			return;
		}
		
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
			compareResults(1, sim1.simulate(null, null), sim2.simulate(null, null), sim1, sim2);
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
			actions1.add(new SimulateAction(75l, SimulateAction.ACTION_TYPE.UPDATE));
			
			Simulation sim1 = new Simulation("simplemap.xml", 1, actions1);
			sim1.simulate(null, null);
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
			List<String> results1 = sim1.simulate(null, null);
			
			
			
			List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
			actions2.add(new SimulateAction(85l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(86l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
			List<String> results2 = sim2.simulate(null, null);
			
			compareResults(3, results1, results2, sim1, sim2);
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
				List<String> results1 = sim1.simulate("log1.txt", null);
				
				
				
				List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
				actions2.add(new SimulateAction(80l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(85l, build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
				actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
				actions2.add(new SimulateAction(50000l, SimulateAction.ACTION_TYPE.UPDATE));
				actions2.add(new SimulateAction(50000l, SimulateAction.ACTION_TYPE.SAVE));
				
				Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
				List<String> results2 = sim2.simulate("log2.txt", null);
				
				
				//saveResultsToFile(results2, "results2.txt");
				compareResults(4, results1, results2, sim1, sim2);
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
						false, new EvenlySpacedSaves(10), null, false
					);
				List<String> l1 = sim1.simulate("test5-gold.txt", null);
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayerbuildafewthings.txt")
						),
						true, new EvenlySpacedSaves(10), null, false
					);
				List<String> l2 = sim2.simulate("test5-modified.txt", null);
				
				compareResults(5, l1, l2, sim1, sim2);
				
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
						false, new EvenlySpacedSaves(3), null, false
					);
				List<String> l1 = sim1.simulate("test6-gold.txt", null);
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/singleplayercrash.txt")
						),
						true, new EvenlySpacedSaves(3), null, false
					);
				List<String> l2 = sim2.simulate("test6-modified.txt", null);
				
				compareResults(6, l1, l2, sim1, sim2);
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
						false, new EvenlySpacedSaves(3), null, false
					);
				List<String> l1 = sim1.simulate("test7-gold.txt", null);
				
				System.out.println("\tRunning part 2...");
				
				Simulation sim2 = loadSimFromFile("simplemap.xml", 1,
						new FileInputStream(
							new File("testcases/test7-singleplayerattackbug.txt")
						),
						true, new EvenlySpacedSaves(3), null, false
					);
				List<String> l2 = sim2.simulate("test7-modified.txt", null);
				
				compareResults(7, l1, l2, sim1, sim2);
			} catch(FileNotFoundException fnfe){System.out.println("FileNotFound for Test 7");}
		}
		
		{
			//Test Case 8 = first multiplayer test
			
			/*
			//actual end time is 281180.  Need protocol replacement to
			//make the full length pass, however.
			twoPlayerTest(8, "simplemap.xml", "testcases/test8-host.txt", "testcases/test8-guest.txt", 250000l, new EvenlySpacedSaves(100));
			
			// Tests based on EOH 2012 data
			
			// both 9 and 10 fail with DecisionCheckExceptions.
			twoPlayerTest(9, "simplemap.xml", "eoh2012logs/Day 1/log1-host.txt", "eoh2012logs/Day 1/log1-guest.txt", 267000, new EvenlySpacedSaves(100));
			twoPlayerTest(10, "simplemap.xml", "eoh2012logs/Day 1/log4-host.txt", "eoh2012logs/Day 1/log4-guest.txt", 255000, new EvenlySpacedSaves(100)); //end at 268000
			
			//passes
			twoPlayerTest(11, "simplemap.xml", "eoh2012logs/Day 1/log8-host.txt", "eoh2012logs/Day 1/log8-guest.txt", 600000, new EvenlySpacedSaves(100));
			twoPlayerTest(12, "simplemap.xml", "eoh2012logs/Day 1/log9-host.txt", "eoh2012logs/Day 1/log9-guest.txt", 341000, new EvenlySpacedSaves(100));
			twoPlayerTest(13, "simplemap.xml", "eoh2012logs/Day 1/log10-host.txt", "eoh2012logs/Day 1/log10-guest.txt", 596000, new EvenlySpacedSaves(100));
			twoPlayerTest(14, "simplemap.xml", "eoh2012logs/Day 1/log11-host.txt", "eoh2012logs/Day 1/log11-guest.txt", 438000, new EvenlySpacedSaves(100)); //ends at 509000.  a lot of decisions are unverified.
			
			//another 3 tests
			twoPlayerTest(15, "simplemap.xml", "eoh2012logs/Day 1/log12-host.txt", "eoh2012logs/Day 1/log12-guest.txt", 580000, new EvenlySpacedSaves(100));
			twoPlayerTest(16, "simplemap.xml", "eoh2012logs/Day 1/log13-host.txt", "eoh2012logs/Day 1/log13-guest.txt", 370000, new EvenlySpacedSaves(100)); //one of these two goes past 800,000.  Also, we have xml problems.
			twoPlayerTest(17, "simplemap.xml", "eoh2012logs/Day 1/log14-host.txt", "eoh2012logs/Day 1/log14-guest.txt", 592000, new EvenlySpacedSaves(100)); //ends at 1216000.  As is, should pass, but for the full time it hits DecisionCheckException
			*/
			
			long[] save_times = {0, 626800, 626900, 627000, 627100, 627200, 627300, 627520};
			twoPlayerTest(18, "simplemap.xml", "testcases/log18-host.txt", "testcases/log18-guest.txt",
					740000, new CustomSaves(save_times)); // goes to 768000

		}
	}
	
	public static void twoPlayerTest(int test_num, String map_file, String input_log1, String input_log2, long total_time, SimSaves saves)
	{
		System.out.println("Running Test " + test_num + "...");
		Simulation sim1, sim2;
		
		try {
			sim1 = loadSimFromFile(map_file, 2,
					new FileInputStream(
						new File(input_log1)
					),
					false, saves, total_time, false
				);

			sim2 = loadSimFromFile(map_file, 2,
					new FileInputStream(
						new File(input_log2)
					),
					false, saves, total_time, false
				);
		} catch(FileNotFoundException fnfe) {
			System.out.println("FileNotFound for Test " + test_num);
			return;
		}
		
		System.out.println("Test " + test_num + " has same orders: " + hasSameOrders(sim1, sim2));
		//correctOrders(sim1, sim2, 281180l);
		
		// decisions made by player 2 will show up in player 1's logfile and vice versa.
		List<Order> decisions2 = sim1.extractDecisions();
		List<Order> decisions1 = sim2.extractDecisions();
		
		List<String> l1 = null, l2 = null;
		boolean finished1 = false;
		boolean finished2 = false;
		boolean decision_check_exc1 = false;
		boolean decision_check_exc2 = false;
		
		try {
			System.out.println("\tRunning part 1...");
			l1 = sim1.simulate("test" + test_num +"-a.txt", new RecordKeeper(decisions1));
			finished1 = true;
		} catch (GameUpdater.DisagreementException e) {
			e.printStackTrace();
		} catch (RecordKeeper.DecisionCheckException e) {
			e.printStackTrace();
			decision_check_exc1 = true;
		}
		
		try {
			System.out.println("\tRunning part 2...");
			l2 = sim2.simulate("test" + test_num + "-b.txt", new RecordKeeper(decisions2));
			finished2 = true;
		} catch (GameUpdater.DisagreementException e) {
			e.printStackTrace();
		} catch (RecordKeeper.DecisionCheckException e) {
			e.printStackTrace();
			decision_check_exc2 = true;
		}		
		
		if (!finished1)
			System.out.println("ERROR: part 1 failed to finish");
		if (!finished2)
			System.out.println("ERROR: part 2 failed to finish");
		
		if (decision_check_exc1 || decision_check_exc2)
			System.out.println("NOTE: we have a decision check exception, so consider disregarding the results.");
		
		if (finished1 && finished2)
		{
			if (!compareResults(test_num, l1, l2, sim1, sim2))
			{
				// Part 3: rerun part 1 with everything in order.
				check_in_order(test_num, 1, map_file, input_log1, saves,
						total_time, decision_check_exc1, l1, sim1, decisions1);
				saveResultsToFile(l1, "test" + test_num + "p1.txt");
				
				// Part 4: rerun part 2 with everything in order.
				check_in_order(test_num, 2, map_file, input_log2, saves,
						total_time, decision_check_exc2, l2, sim2, decisions2);
				saveResultsToFile(l2, "test" + test_num + "p2.txt");
			}
		}
	}

	public static void check_in_order(
			int test_num,
			int part_num,
			String map_file,
			String input_log,
			SimSaves saves,
			long total_time,
			boolean decision_check_exc,
			List<String> original_results,
			Simulation original_sim,
			List<Order> decisions_to_check
		)
	{
		List<String> cl = null;
		Simulation sim_check = null;
		boolean finished_cl = false;
		
		try {
			sim_check = loadSimFromFile(map_file, 2,
				new FileInputStream(
					new File(input_log)
				),
				true, saves, total_time, true
			);
		} catch (FileNotFoundException fnfe) {
			System.err.println("Failed to find file " + input_log);
			return;
		}
		
		try {
			System.out.println("\tRunning part " + part_num + " in order...");
			cl = sim_check.simulate("test" + test_num + "-c" + part_num + ".txt", new RecordKeeper(decisions_to_check));
			finished_cl = true;
		} catch (GameUpdater.DisagreementException e) {
			e.printStackTrace();
		} catch (RecordKeeper.DecisionCheckException e) {
			e.printStackTrace();
			
			if (decision_check_exc)
				System.out.println("Both part " + part_num + " and the check terminated with DecisionCheckExceptions.");
		}
		
		if (!finished_cl)
			System.out.println("ERROR: part " + part_num + " (in order) failed to finish");
		else
		{
			compareResults(test_num, original_results, cl, original_sim, sim_check);
			saveResultsToFile(cl, "test" + test_num + "p" + part_num + "_check.txt");
		}
	}
	
	public static boolean compareResults(int test_num, List<String> l1, List<String> l2, Simulation sim1, Simulation sim2)
	{
		System.out.println("Comparing for test "+test_num);
		
		boolean identical = true;
		if(l1.size() != l2.size())
		{
			System.out.println("\tunequal result lengths in test " + test_num);
			System.out.println("\tList 1: " + l1.size() +"; List 2: " + l2.size());
			return false;
		}
		else
		{
			for(int i=0; i < l1.size(); ++i)
			{
				boolean match = l1.get(i).equals(l2.get(i));
				if(!match)
				{
					//TODO: remove hackiness
					//Parsing the time out of here is a little hacky, but it gets the job done.
					String savept_str = l1.get(i).substring(0, l1.get(i).indexOf('\n'));
					String time_str = savept_str.substring(savept_str.indexOf("@")+1);
					Long save_time = Long.parseLong(time_str, 10);
					
					if (hasSameOrdersUpToAction(sim1, sim2, sim1.getSaveAt(save_time), sim2.getSaveAt(save_time)))
					{
						System.out.println("\tSave point " + i + " does not match: " + savept_str);
					}
					else
					{
						System.out.println("\tSave point " + i + " at time " + time_str + " has different orders, skipping.\n");
						match = true; //suppress difference
					}
				}
				else
					System.out.println("\tSave point " + i + " matches: " + l2.get(i).substring(0, l2.get(i).indexOf('\n')));
				identical = identical && match;
			}
			
			if(identical)
				System.out.println("\ttest PASSED");
			else
				System.out.println("\ttest FAILED");
			
			return identical;
		}
	}
	
	public static boolean hasSameOrders(Simulation sim1, Simulation sim2)
	{
		return hasSameOrdersUpToAction(sim1, sim2, null, null);
	}
	
	public static boolean hasSameOrdersUpToAction(
			Simulation sim1,
			Simulation sim2,
			SimulateAction last_action_sim1,
			SimulateAction last_action_sim2
		)
	{
		Set<Order> o1 = new HashSet<Order>();
		Set<Order> o2 = new HashSet<Order>();
		
		for (SimulateAction action : sim1.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER && 
					(last_action_sim1 == null || action.number <= last_action_sim1.number) )
			{
				boolean retval = o1.add(action.the_order);
				if (!retval)
					throw new RuntimeException("sim1 has a duplicate order!");
			}
		}
		
		for (SimulateAction action : sim2.actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER &&
					(last_action_sim2 == null || action.number <= last_action_sim2.number) )
			{
				boolean retval = o2.add(action.the_order);
				if (!retval)
					throw new RuntimeException("sim2 has a duplicate order!");
			}
		}
		
		boolean same_orders = o1.containsAll(o2) && o2.containsAll(o1);
		
		if (!same_orders)
			System.out.println("Missing orders detected");
		
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
		
		return same_orders;
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
		
		sim1.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.UPDATE, SimulateAction.ORDER_TYPE.NONE_SPECIFIED));
		sim1.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.SAVE, SimulateAction.ORDER_TYPE.NONE_SPECIFIED));
		sim2.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.UPDATE, SimulateAction.ORDER_TYPE.NONE_SPECIFIED));
		sim2.actions.add(new SimulateAction(end_time+1, null, SimulateAction.ACTION_TYPE.SAVE, SimulateAction.ORDER_TYPE.NONE_SPECIFIED));
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
											 SimSaves save_points,
											 Long end_time,
											 boolean orders_at_actual_times)
	{
		// Load actions from given file
		List<SimulateAction> actions = loadActionsFromFile(file);

		// Find last update & gather all actions we want
		// to use into new_actions
		List<SimulateAction> new_actions = filterActions(actions, remove_updates, end_time);
	
		// find end time
		if (end_time == null)
			end_time = findEndTime(actions);
		
		// add save points throughout the sim
		new_actions.addAll(save_points.updatesAndSaves(end_time));
		
		// adjust to actual times if requested (throw out network lag)
		if (orders_at_actual_times)
		{
			moveOrdersToActualTimes(new_actions);
		}
		
		return new Simulation(map, num_players, new_actions);
	}
	
	public static abstract class SimSaves
	{
		public abstract List<Long> savePoints(long end_time);
		
		public List<SimulateAction> updatesAndSaves(long end_time)
		{
			List<SimulateAction> actions = new ArrayList<SimulateAction>();
			
			for (Long t : savePoints(end_time))
			{
				actions.add(new SimulateAction(t, SimulateAction.ACTION_TYPE.UPDATE));
				actions.add(new SimulateAction(t, SimulateAction.ACTION_TYPE.SAVE));
			}
			
			return actions;
		}
	}
	
	public static class EvenlySpacedSaves extends SimSaves
	{
		int num_updates_and_saves;
		
		public EvenlySpacedSaves(int num_updates_and_saves)
		{
			this.num_updates_and_saves = num_updates_and_saves;
		}
		
		public List<Long> savePoints(long end_time)
		{
			List<Long> saves = new ArrayList<Long>();
			
			for(int i=1; i <= num_updates_and_saves; i++)
			{
				saves.add(end_time*i/num_updates_and_saves);
			}
			
			return saves;
		}
	}
	
	public static class CustomSaves extends SimSaves
	{
		long[] save_times;
		
		public CustomSaves(long[] save_times)
		{
			this.save_times = save_times;
		}
		
		public List<Long> savePoints(long end_time)
		{
			List<Long> saves = new ArrayList<Long>();
			for (long s : save_times)
			{
				saves.add(s);
			}
			saves.add(end_time);
			
			return saves;
		}
	}
	
	private static List<SimulateAction> loadActionsFromFile(InputStream file)
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
		
		return actions;
	}
	
	private static long findEndTime(List<SimulateAction> sorted_actions)
	{
		SimulateAction last = null;

		// Find last update & gather all actions we want
		// to use into new_actions
		for (int i=0; i < sorted_actions.size(); i++)
		{
			SimulateAction action = sorted_actions.get(i);
			
			if (action.type.equals(SimulateAction.ACTION_TYPE.UPDATE))
			{
				// handle updates specially
				last = action; //actions are ordered, so this is new candidate for the last update.
			}
		}
		
		return last.do_at_time;
	}
	
	private static List<SimulateAction> filterActions(List<SimulateAction> actions, boolean remove_updates, Long end_time)
	{
		List<SimulateAction> new_actions = new ArrayList<SimulateAction>();

		for (int i=0; i < actions.size(); i++)
		{
			SimulateAction action = actions.get(i);
			
			//destroy anything happening after end_time.
			if (end_time != null && action.do_at_time > end_time)
			{
				continue;
			}
			else if (action.type.equals(SimulateAction.ACTION_TYPE.UPDATE))
			{
				if (!remove_updates)
					new_actions.add(action);
			}
			else
			{
				new_actions.add(action);
			}
		}
		
		return new_actions;
	}
	
	private static void moveOrdersToActualTimes(List<SimulateAction> actions)
	{
		for (SimulateAction action : actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.SCHEDULE_ORDER)
			{
				action.do_at_time = action.the_order.scheduled_time - 1;
			}
		}
	}
}
