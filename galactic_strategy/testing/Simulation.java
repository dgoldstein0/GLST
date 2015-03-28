package galactic_strategy.testing;
import galactic_strategy.GameControl;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Message;
import galactic_strategy.ui.GameInterface;
import galactic_strategy.user_actions.Order;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulation
{
	final int num_players;
	List<SimulateAction> actions;
	List<Long> save_pts;
	String map_location;
	
	/** Simulation constructor
	 * 
	 * @param map_location name of the map file
	 * @param num_players number of players in this sim
	 * @param actions list of SimulateActions to run through in the simulation.
	 * 		N.B.: this constructor will sort actions (so it is safe to pass it an unsorted
	 * 		list - but it WILL be modified).
	 * @param saves the list of times to save at.
	 */
	public Simulation(String map_location, int num_players, List<SimulateAction> actions, List<Long> saves)
	{
		// Add an update at the last save point, to make sure we actually run that long.
		if (saves.size() != 0)
			actions.add(new SimulateAction(saves.get(saves.size()-1), SimulateAction.ACTION_TYPE.UPDATE));
		
		this.num_players = num_players;
		this.actions = actions;
		this.map_location = map_location;
		save_pts = saves;
		
		// number them before sorting - this forces the sort to be a stable sort.
		numberActions();
		Collections.sort(actions, new SimulateAction.Comparer());
	}

	private void numberActions()
	{
		for (int i = 0; i < actions.size(); i++)
		{
			actions.get(i).number = i;
		}
	}
			
	List<String> simulate(String logfile_name, List<Order> decisions)
	{
		RecordKeeper checker = new RecordKeeper(decisions, save_pts);
		GameControl GC = new GameControl(null, checker);
		GameInterface.GC = GC;
		GC.startTest(num_players, true, new File(map_location));
		GC.updater.setupLogFile((logfile_name == null) ? "log.txt" : logfile_name);
		
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
				case RECEIVED_DECISION:
					GC.updater.decideOrder(new Message(Message.Type.DECISION, action.the_order));
					break;
			}
		}
		GC.updater.logFile.close();
		
		return checker.getSavedResults();
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
