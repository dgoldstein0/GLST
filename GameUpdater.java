import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameUpdater {

	static final boolean DEBUGGING = true;
	
	TimeManager TC;
	final GameControl GC;
	final PriorityBlockingQueue<Order> pending_execution;
	final BlockingQueue<Message> incoming_decisions;
	final HashMap<Order, Order.Decision> decisions;
	SortedSet<Order> already_executed;
	SortedSet<Order> ready_to_retire;
	HashMap<Integer, Long> most_recent_time; //tracking for garbage collection; maps player_id->last order received
	TaskManager TM;
	
	long last_time_updated;
	
	boolean is_closing;
	XMLEncoder logFile;
	Object log_lock;
	
	public GameUpdater(GameControl ctrl)
	{
		pending_execution = new PriorityBlockingQueue<Order>();
		already_executed = new TreeSet<Order>();
		ready_to_retire = new TreeSet<Order>();
		incoming_decisions = new LinkedBlockingQueue<Message>();
		decisions = new HashMap<Order, Order.Decision>();
		TM = new TaskManager();
		GC = ctrl;
		last_time_updated = 0l;
		log_lock = new Object();
		most_recent_time = new HashMap<Integer, Long>();
	}
	
	public void setTimeManager(TimeManager TM)
	{
		TC=TM;
	}
	
	public void startUpdating()
	{
		is_closing=false;
		
		//this initialization of most_recent_time has to wait until GC.players
		//is populated, so it cannot be done in the GameUpdater constructor.
		for (int i=0; i < GC.players.length; i++)
		{
			if(GC.players[i] != null)
			{
				most_recent_time.put(i, 0l);
			}
		}
		
		TM.startConstIntervalTask(new Updater(),(int)GalacticStrategyConstants.TIME_GRANULARITY);
	}
	
	public void stopUpdating()
	{
		TM.stopTask();
		
		//TODO: debugging code, should later be removed
		synchronized(log_lock)
		{
			if(logFile != null)
			{
				logFile.close();
				logFile = null;
				is_closing = true;
			}
		}
	}
	
	public long getTime()
	{
		return TC.getTime();
	}
	
	/**scheduleOrder
	 * 
	 * Adds an order to the priority queue pending_execution.  Note that this does NOT
	 * notify other computers of the order - GameControl.scheduleOrder does that
	 * 
	 * @param o the order to add to the queue
	 * */
	public void scheduleOrder(Order o)
	{
		pending_execution.add(o);
	}
	
	public void decideOrder(Message m)
	{
		incoming_decisions.add(m);
	}
	
	private class Updater extends TimerTask
	{
		private Updater(){}
		
		public void run()
		{
			try
			{
				updateGame();
			}
			catch (DataSaverControl.DataNotYetSavedException e)
			{
				//TODO: work on exception handling
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}
	
	public void updateGame() throws DataSaverControl.DataNotYetSavedException
	{
		long time_elapsed, update_to;
		PriorityQueue<Order> local_pending_execution;
		
		//Atomically get the time and dequeue orders.
		//This way, if we have an order with the same time as time_elapsed
		//we will *know* whether it was sucked into this update cycle or not
		//by their ordering in the log.  Without the locking, we cannot
		//be sure whether it was dequeued here or not.
		synchronized(log_lock)
		{
			time_elapsed=TC.getTime();
			
			//TODO: Debugging code, remove later
			log(new GameSimulator.SimulateAction(time_elapsed, GameSimulator.SimulateAction.ACTION_TYPE.UPDATE));
			
			update_to=getLast_time_updated();
			//System.out.println("Updating to time_elapsed=" + Long.toString(time_elapsed));
			//start events that need to occur before time_elapsed
			
			//can safely use unsynchronized version here since this is only used by the current thread
			local_pending_execution = new PriorityQueue<Order>();
			
			do
			{
				Order o = pending_execution.peek();
				if(o != null && o.scheduled_time <= time_elapsed)
					local_pending_execution.add(pending_execution.remove()); //if this does not remove o, it removes one just inserted earlier than o.
				else
					break; /*small chance an order should be removed and executed this time around but isn't, 
							if we don't see it with peek.  it will be executed next time through updateGame,
							though potentially with a bit of reversion*/
			} while(true);
		}
		
		/*
		 * now figure out the earliest order in local_pending_execution, and make that time update_to,
		 * if update_to is currently larger
		 */
		Order first_order = local_pending_execution.peek();
		if(first_order != null)
		{
			long next_order_time = first_order.scheduled_time;
			if(next_order_time < update_to)	
			{
				//forces the main loop to reconsider below
				
				//set to update to the time at which this order will be executed
				update_to = TimeControl.roundUpToTimeGrain(next_order_time);
				long revert_to_time = update_to - GalacticStrategyConstants.TIME_GRANULARITY;
				
				//revert to data for time grain before the given order is executed.
				GC.map.revertAllToTime(revert_to_time, GC.players);
				
				//and now figure out which orders to replay, and queue those
				//too.
				Order temp = new InvalidOrder();
				temp.scheduled_time = revert_to_time+1;
				temp.p_id = -1;
				temp.order_number = -1;
				
				SortedSet<Order> orders_to_redo = already_executed.tailSet(temp);
				local_pending_execution.addAll(orders_to_redo);
				orders_to_redo.clear();
			}
		}
		
		//update all planets, facilities, ships and missiles
		for(; update_to <= time_elapsed; update_to+=GalacticStrategyConstants.TIME_GRANULARITY)
		{
			setLast_time_updated(update_to);
			
			/**update all intersystem data.*/
			for(int i=0; i<GC.players.length; i++)
			{
				if(GC.players[i] != null)
				{
					Iterator<Ship> ship_it = GC.players[i].ships_in_transit.iterator();
					Ship s;
					while(ship_it.hasNext())
					{
						s=ship_it.next();
						s.moveDuringWarp(update_to, ship_it); //the iterator is passed so that moveDuringWarp can remove the ship from the iteration, and by doing so from ships_in_transit
					}
				}
			}
			
			for(GSystem sys : GC.map.systems)
			{
				/* We must stick facilities and planets in this loop because
				 * otherwise Ship updating would not be coordinated with
				 * facilities and planets, so bugs could then occur in terms
				 * of building ships or invading planets.
				 * 
				 * For instance, consider this scenario: ship is attacking a
				 * Shipyard, and Shipyard is about to complete a new Ship right
				 * around the time it is about to explode.  If it should finish
				 * the ship after it is destroyed, but the call to updateGame
				 * handles both the time in which the Shipyard will be
				 * destroyed and the time at which the ship would be completed
				 * if the shipyard were not destroyed, and if facilities &
				 * planets were updated before the Ships all the way to
				 * time_elapsed, then the Shipyard could be updated to a time
				 * later than the time it is destroyed at, produce the ship
				 * which it should not produce, and then be destroyed.  But now
				 * we have a ship which should have not completed construction.
				 * Uh oh...
				 * 
				 * Though less dramatic, similar issues can exist with
				 * mining/taxation.  Thats why planets and facilities get
				 * update in here.
				 */
				
				//update planets/facilities:
				for(Satellite<?> sat : sys.orbiting)
				{
					//Q: do we really need planet/moon positions in here?
					//Once we have ships flying around, some planets will need
					//this calculation anyway.  Without good caching, we might
					//do the move calculations several times.  So it is
					//probably better for performance to go here.
					
					sat.orbit.move(update_to);
					if(sat instanceof Planet)
					{
						((Planet)sat).update(update_to);
						for(Satellite<?> sat2 : ((Planet)sat).orbiting)
						{
							sat2.orbit.move(update_to);
							if(sat2 instanceof Moon)
							{
								((Moon)sat2).update(update_to);
							}
						}
					}
				}
				
				//update data for all ships
				for(int i=0; i<sys.fleets.length; i++)
				{
					synchronized(sys.fleets[i].lock)
					{
						Fleet.ShipIterator ship_iteration = sys.fleets[i].iterator();
						for(Ship.ShipId j; ship_iteration.hasNext();)
						{
							j=ship_iteration.next();
							Ship s = sys.fleets[i].ships.get(j);
							s.update(update_to, ship_iteration);
						}
					}
				}
				
				//update all missiles AND save data
				synchronized(sys.missiles)
				{
					Iterator<Missile.MissileId> missile_iteration = sys.missiles.keySet().iterator();
					for(Missile.MissileId i; missile_iteration.hasNext();)
					{
						i=missile_iteration.next();
						sys.missiles.get(i).update(update_to, missile_iteration); //returns true if the missile detonates
					}
				}
				
				//collision processing... (FAIL)
				detectCollisions(sys, update_to);
			}
			
			Order o;
			while( (o = local_pending_execution.peek()) != null && o.scheduled_time <= update_to)
			{
				/**execute does all the necessary reversion itself.  It never reverts anything to earlier than
				 * scheduled_time, which should be within one time grain less than update_to*/
				Order cur_order = local_pending_execution.remove();
				cur_order.execute(GC.map);
				
				already_executed.add(cur_order);
				most_recent_time.put(cur_order.p_id, cur_order.scheduled_time);
			}
			
			/**
			 * Save Everything here!
			 * Everything = map data (ships, missiles, facilities, planets,
			 * etc.) and Player data (money, metal, and ships in transit).
			 */
			GC.map.saveAllData(GC.players);
		}
		
		setLast_time_updated(update_to);
		
		//This can flare up in the case that time_elapsed >= order time, but the last time grain
		//run here happens to be before order time since both don't have to land on a time grain
		//(at least, for some orders).  I.e. time_elapsed=56, order @ 53, will only update through
		// time 40 (time_grain is 20)
		if(!local_pending_execution.isEmpty())
		{
			System.out.println("We still have orders in the local queue.");
			pending_execution.addAll(local_pending_execution);
		}
		
		//Collect the Trash!
		
		//Read off the incoming_decisions queue, and put the info into the
		//almighty decisions HashMap.
		while (!incoming_decisions.isEmpty())
		{
			Message m = incoming_decisions.remove();
			Order o = m.contents;
			decisions.put(o, o.decision); //TODO: this only works for the two-player case.  For more players, we need a counting mechanism.

			if (most_recent_time.get(m.sender_id) < o.scheduled_time)
				most_recent_time.put(m.sender_id, o.scheduled_time);
		}
		
		{
			long minimum = Long.MAX_VALUE;
			for (Integer i : most_recent_time.keySet())
			{
				long last_order_time = most_recent_time.get(i);
				if (last_order_time < minimum)
					minimum = last_order_time;
			}
			
			//now every already_executed order with time less than minimum is trash.
			InvalidOrder min_order = new InvalidOrder();
			min_order.scheduled_time = minimum;
			min_order.order_number = 0;
			min_order.p_id = -1;
			SortedSet<Order> will_retire = already_executed.headSet(min_order);
			ready_to_retire.addAll(will_retire);
			
			for (Order o : will_retire)
			{
				GC.notifyAllPlayersOfDecision(o);
			}
			
			will_retire.clear();
		}
		
		for (Iterator<Order> it = ready_to_retire.iterator(); it.hasNext();)
		{
			Order o = it.next();
			if (decisions.get(o) != null)
			{
				Order.Decision d = decisions.get(o);
				
				//TODO: this only works in the 2 player case
				if (d.equals(o.decision))
				{
					it.remove();
					decisions.remove(o);
				}
				else
				{
					throw new RuntimeException("Disagreement Detected!\n" + o.getClass().getName() + " from player " + o.p_id + ", order_num " + o.order_number + " scheduled at time " + o.scheduled_time);
				}
			}
		}
		
		SwingUtilities.invokeLater(new InterfaceUpdater(time_elapsed));
	}
	
	private void detectCollisions(GSystem sys, long t) throws DataSaverControl.DataNotYetSavedException
	{
		for(int i=0; i<sys.fleets.length; i++)
		{
			synchronized(sys.fleets[i].lock)
			{
				Fleet.ShipIterator ship_iterator1 = sys.fleets[i].iterator();
				for(Ship.ShipId id1; ship_iterator1.hasNext();)
				{
					id1=ship_iterator1.next();
					for(int j=i; j<sys.fleets.length; j++)
					{
						synchronized(sys.fleets[j].lock)
						{
							Fleet.ShipIterator ship_iterator2 = sys.fleets[i].iterator();
							for(Ship.ShipId id2; ship_iterator2.hasNext();)
							{
								id2=ship_iterator2.next();
								if(!id1.equals(id2))
								{
									Ship a = sys.fleets[i].ships.get(id1);
									Ship b = sys.fleets[j].ships.get(id2);
									
									if(a != null && b != null)
										doCollision(a,b,t);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void doCollision(Ship a, Ship b, long t) throws DataSaverControl.DataNotYetSavedException
	{
		double x_a = a.pos_x;
		double y_a = a.pos_y;
		
		double x_b = b.pos_x;
		double y_b = b.pos_y;
		
		double dif_x = x_a - x_b;
		double dif_y = y_a - y_b;
		double len_sq_dif = dif_x*dif_x + dif_y*dif_y;
		
		double a_dim = a.type.dim * a.type.img.scale;
		double b_dim = b.type.dim * b.type.img.scale;
		double collision_dist = (a_dim + b_dim) / 2.0;
		
		if(len_sq_dif < collision_dist*collision_dist)
		{
			//do collision
			
			//velocity a -= 2*projection onto dif
			double v_x_a = a.speed*Math.cos(a.direction);
			double v_y_a = a.speed*Math.sin(a.direction);
			
			double proj_frac_a;
			proj_frac_a = (v_x_a*dif_x + v_y_a*dif_y)/len_sq_dif;
			
			//velocity b += 2*projection onto dif
			double v_x_b = b.speed*Math.cos(b.direction);
			double v_y_b = b.speed*Math.sin(b.direction);
			
			double proj_frac_b;
			proj_frac_b = (v_x_b*dif_x + v_y_b*dif_y)/len_sq_dif;
			
			if (proj_frac_a < 0.0) //negative indicates ship a is moving in the direction of ship b
			{
				a.speed = 0.0;
			}
			
			if(proj_frac_b > 0.0)
			{
				b.speed = 0.0;
			}
		}
	}
	
	public class InterfaceUpdater implements Runnable
	{
		long time;
		
		public InterfaceUpdater(long t){time=t;}
		
		public void run()
		{
			GC.updateInterface(time);
		}
	}
	
	public void setupLogFile(String logname)
	{
		try {
			logFile = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(logname)));
			logFile.setExceptionListener(new MyExceptionListener());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void log(Object o)
	{
		synchronized(log_lock)
		{
			if(!is_closing)
			{
				if(logFile == null)
				{
					String logname = JOptionPane.showInputDialog("What should the log file be named?");
					setupLogFile(logname);
				}
				
				if(o != null)
				{
					logFile.writeObject(o);
				}
			}
		}
	}
	
	public static class MyExceptionListener implements ExceptionListener
	{
		@Override
		public void exceptionThrown(Exception arg0) {
			arg0.printStackTrace();
		}
	}
	
	public long getLast_time_updated(){return last_time_updated;}
	public void setLast_time_updated(long t){last_time_updated=t;}
}
