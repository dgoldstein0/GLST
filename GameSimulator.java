import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
			compareResults(1, sim1.simulate(), sim2.simulate());
		}
		
		{
			System.out.println("Running Test 2...");
			//test case 2: verifying my orders-left-in-queue theory, and
			
			FacilityBuildOrder shipyard_build_order = new FacilityBuildOrder();
			shipyard_build_order.setBldg_type(FacilityType.SHIPYARD);
			shipyard_build_order.setPlayer_id(0);
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
			sim1.simulate();
		}
		
		{
			System.out.println("Running Test 3...");
			//test time-independence with FacilityBuildOrder
			
			FacilityBuildOrder shipyard_build_order = new FacilityBuildOrder();
			shipyard_build_order.setBldg_type(FacilityType.SHIPYARD);
			shipyard_build_order.setPlayer_id(0);
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
			List<String> results1 = sim1.simulate();
			
			
			
			List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
			actions2.add(new SimulateAction(85l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(86l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
			List<String> results2 = sim2.simulate();
			
			compareResults(3, results1, results2);
			//saveResultsToFile(results1, "test3p1.txt");
			//saveResultsToFile(results2, "test3p2.txt");
		}
		
		{
			//Test Case 4
			System.out.println("Running Test 4...");
			//test time-independence with FacilityBuildOrder
			
			FacilityBuildOrder shipyard_build_order = new FacilityBuildOrder();
			shipyard_build_order.setBldg_type(FacilityType.SHIPYARD);
			shipyard_build_order.setPlayer_id(0);
			SatelliteDescriber<Planet> eulenspiegel_desc = new SatelliteDescriber<Planet>();
				GSystemDescriber azha_sys = new GSystemDescriber();
					azha_sys.setId(1);
				eulenspiegel_desc.setBoss_describer(azha_sys);
				eulenspiegel_desc.setId(0);
			shipyard_build_order.setSat_desc(eulenspiegel_desc);
			
			shipyard_build_order.setScheduled_time(80l); //note the time here
			
			
			List<SimulateAction> actions1 = new ArrayList<SimulateAction>();
			actions1.add(new SimulateAction(0l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions1.add(new SimulateAction(80l,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(100l,SimulateAction.ACTION_TYPE.SAVE));
			long[] random_times = {198l,235l,240l,400l,405l,509l,737l,801l,840l,874l,940l,1000l};
			for(long num : random_times)
				actions1.add(new SimulateAction(num,SimulateAction.ACTION_TYPE.UPDATE));
			actions1.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim1 = new Simulation("simplemap.xml", 1, actions1);
			List<String> results1 = sim1.simulate();
			
			
			
			List<SimulateAction> actions2 = new ArrayList<SimulateAction>();
			actions2.add(new SimulateAction(80l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(80l, shipyard_build_order, SimulateAction.ACTION_TYPE.SCHEDULE_ORDER));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(100l, SimulateAction.ACTION_TYPE.SAVE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.UPDATE));
			actions2.add(new SimulateAction(1000l, SimulateAction.ACTION_TYPE.SAVE));
			
			Simulation sim2 = new Simulation("simplemap.xml", 1, actions2);
			List<String> results2 = sim2.simulate();
			
			compareResults(4, results1, results2);
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
					System.out.println("\tSave point " + i + " does not match");
				else
					System.out.println("\tSave point " + i + " matches");
				identical = identical && match;
			}
			
			if(identical)
				System.out.println("\ttest PASSED");
			else
				System.out.println("\ttest FAILED");
		}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				
		List<String> simulate()
		{
			GameControl GC = new GameControl(null);
			GameInterface.GC = GC;
			GC.startTest(num_players, true, new File(map_location));
			
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case SCHEDULE_ORDER:
						GC.updater.pending_execution.add(action.the_order);
						break;
					case INSTANT_ORDER:
						action.the_order.doInstantly(GC.map);
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
				}
			}
			return results;
		}
	}
	
	public static class SimulateAction
	{
		public static enum ACTION_TYPE{UPDATE,SCHEDULE_ORDER,INSTANT_ORDER,SAVE;}
		
		final long do_at_time;
		final Order the_order;
		final ACTION_TYPE type;
		
		public SimulateAction(long time, Order o, ACTION_TYPE t)
		{
			do_at_time = time;
			the_order = o;
			type=t;
		}
		
		public SimulateAction(long time, ACTION_TYPE t)
		{
			do_at_time = time;
			type = t;
			the_order = null;
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
