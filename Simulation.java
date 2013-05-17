import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulation
{
	final int num_players;
	List<SimulateAction> actions;
	String map_location;
	
	/** Simulation constructor
	 * 
	 * @param map_location name of the map file
	 * @param num_players number of players in this sim
	 * @param actions list of SimulateActions to run through in the simulation.
	 * 		N.B.: this constructor will sort actions (so it is safe to pass it an unsorted
	 * 		list - but it WILL be modified).
	 */
	public Simulation(String map_location, int num_players, List<SimulateAction> actions)
	{
		Collections.sort(actions, new SimulateAction.Comparer());
		
		this.num_players = num_players;
		this.actions = actions;
		this.map_location = map_location;
		
		numberActions();
	}
	
	public SimulateAction getSaveAt(Long save_time) {
		
		for (SimulateAction a : actions)
		{
			if (a.type == SimulateAction.ACTION_TYPE.SAVE && a.do_at_time == save_time)
				return a;
		}
		
		throw new RuntimeException("Save at time " + save_time + " doesn't exist.");
	}

	private void numberActions()
	{
		for (int i = 0; i < actions.size(); i++)
		{
			actions.get(i).number = i;
		}
	}
			
	List<String> simulate(String logfile_name, RecordKeeper checker)
	{
		GameControl GC = new GameControl(null, checker);
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
	
	/**
	 * This function finds all decisions that the simulation receives, and packages them into a list
	 * 
	 * TODO: consider adding filtering based one which players the decisions are coming from
	 * 
	 * @return the list of orders that are decided
	 */
	List<Order> extractDecisions(){
		
		List<Order> decided = new ArrayList<Order>();
		
		for (SimulateAction action : actions)
		{
			if (action.type == SimulateAction.ACTION_TYPE.RECEIVED_DECISION)
			{
				decided.add(action.the_order);
			}
		}
		
		return decided;
	}
}
