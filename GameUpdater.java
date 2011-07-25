import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;
import javax.swing.SwingUtilities;

public class GameUpdater {

	static final boolean DEBUGGING = true;
	
	TimeManager TC;
	final GameControl GC;
	final PriorityBlockingQueue<Order> pending_execution;
	TaskManager TM;
	
	long last_time_updated;
	
	boolean is_closing;
	XMLEncoder logFile;
	Object log_lock;
	
	public GameUpdater(GameControl ctrl)
	{
		pending_execution = new PriorityBlockingQueue<Order>();
		TM = new TaskManager();
		GC = ctrl;
		last_time_updated = 0l;
		log_lock = new Object();
	}
	
	public void setTimeManager(TimeManager TM)
	{
		TC=TM;
	}
	
	public void startUpdating()
	{
		is_closing=false;
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
	 * Adds an order to the priority queue pending_execution.  Note that this does NOT
	 * notify other computers of the order - GameControl.scheduleOrder does that
	 * 
	 * @param o the order to add to the queue
	 * */
	public void scheduleOrder(Order o)
	{
		pending_execution.add(o);
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
		long time_elapsed=TC.getTime();
		
		//TODO: Debugging code, remove later
		log(new GameSimulator.SimulateAction(time_elapsed, GameSimulator.SimulateAction.ACTION_TYPE.UPDATE));
		
		long update_to=getLast_time_updated();
		//System.out.println("Updating to time_elapsed=" + Long.toString(time_elapsed));
		//start events that need to occur before time_elapsed
		
		//can safely use unsynchronized version here since this is only used by the current thread
		PriorityQueue<Order> local_pending_execution = new PriorityQueue<Order>();
		
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
		
		/*now figure out the earliest order in local_pending_execution, and make that time update_to,
		 * if update_to is currently larger*/
		Order first_order = local_pending_execution.peek();
		if(first_order != null)
		{
			long next_order_time = first_order.scheduled_time;
			if(next_order_time < update_to)
				update_to = TimeControl.roundDownToTimeGrain(next_order_time); //forces the main loop to reconsider below
		}
		
		/**TODO: Revert here!*/
		
		
		/* update positions of all planets/moons/etc.  These are time-independent, and
		 * will also be calculated for other times that we need to know*/
		for(GSystem sys : GC.map.systems)
		{
			//move all planets
			for(Satellite<?> sat : sys.orbiting)
			{
				if(sat instanceof Planet)
				{ //TODO: recursive move?
					for(Satellite<?> sat2 : ((Planet)sat).orbiting)
					{
						sat2.orbit.move(time_elapsed);
					}
				}
				sat.orbit.move(time_elapsed);
			}
		}
		
		//update all planets, facilities, ships and missiles
		for(; update_to <= time_elapsed; update_to+=GalacticStrategyConstants.TIME_GRANULARITY)
		{
			setLast_time_updated(update_to);
			
			/**TODO: Save Everything here!*/
			
			
			/**update all intersystem data.  This is must be within the loop in case ships are reverted back
			 * into warp or something of the sort*/
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
				/*We must stick facilities and planets in this loop because otherwise Ship updating would not
				be coordinated with facilities and planets, so bugs could then occur in terms of building ships
				or invading planets.
				
				For instance, consider this scenario: ship is attacking a Shipyard, and Shipyard is about to
				complete a new Ship right around the time it is about to explode.  If it should finish
				the ship after it is destroyed, but a call to updateGame must handle both the time in which
				the Shipyard will be destroyed and in which the ship would be completed if the shipyard were
				not destroyed, and if facilities/planets were updated before the Ships all the way to
				time_elapsed, then the Shipyard could be updated to a time later than the time it is
				destroyed at, produce the ship which it should not produce, and then be destroyed.  But now
				we have a ship which should have not completed construction.   Uh oh...
				
				Though less dramatic, similar issues can exist with mining/taxation.  So it all goes in here.*/
				
				//update planets/facilities:
				for(Satellite<?> sat : sys.orbiting)
				{
					if(sat instanceof Planet)
					{
						((Planet)sat).update(update_to);
						for(Satellite<?> sat2 : ((Planet)sat).orbiting)
						{
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
							sys.fleets[i].ships.get(j).update(update_to, ship_iteration);
						}
					}
				}
				
				//NOTE: Missile collision detection relies on Missiles being updated after ships.  See Missile.collidedWithTarget
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
				detectCollisions(sys, update_to, local_pending_execution);
			}
			
			Order o;
			while( (o = local_pending_execution.peek()) != null && o.scheduled_time <= update_to)
			{
				/**execute does all the necessary reversion itself.  It never reverts anything to earlier than
				 * scheduled_time, which should be within one time grain less than update_to*/
				local_pending_execution.addAll(local_pending_execution.remove().execute(GC.map));
			}
			
			if(getLast_time_updated() > update_to)
				update_to = getLast_time_updated();
			
			/* debuging code, should later be removed
			if(DEBUGGING)
			{
				log("\r\nUpdated to " + update_to, GC.map);
				log("\n",GC.players);
			}*/
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
		
		SwingUtilities.invokeLater(new InterfaceUpdater(time_elapsed));
	}
	
	private void detectCollisions(GSystem sys, long t, PriorityQueue<Order> local_pending_execution) throws DataSaverControl.DataNotYetSavedException
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
										local_pending_execution.addAll(doCollision(a,b,t-GalacticStrategyConstants.TIME_GRANULARITY));
								}
							}
						}
					}
				}
			}
		}
	}
	
	public Set<Order> doCollision(Ship a, Ship b, long t) throws DataSaverControl.DataNotYetSavedException
	{
		double x_a = a.getXCoord(t);
		double y_a = a.getYCoord(t);
		
		double x_b = b.getXCoord(t);
		double y_b = b.getYCoord(t);
		
		double dif_x = x_a - x_b;
		double dif_y = y_a - y_b;
		double len_sq_dif = dif_x*dif_x + dif_y*dif_y;
		
		double a_dim = a.type.dim * a.type.img.scale;
		double b_dim = b.type.dim * b.type.img.scale;
		double collision_dist = (a_dim + b_dim) / 2.0;
		
		if(len_sq_dif < collision_dist*collision_dist)
		{
			//do collision
			Set<Order> orders = new HashSet<Order>();
			orders.addAll(a.data_control.revertToTime(t));
			orders.addAll(b.data_control.revertToTime(t));
			
			//velocity a -= 2*projection onto dif
			double v_x_a = a.getXVel(t);
			double v_y_a = a.getYVel(t);
			
			double new_v_x_a, new_v_y_a, proj_frac_a;
			proj_frac_a = (v_x_a*dif_x + v_y_a*dif_y)/len_sq_dif;
			if (proj_frac_a < 0.0)
			{
				new_v_x_a = 0.0;//v_x_a - proj_frac_a*dif_x;
				new_v_y_a = 0.0;//v_y_a - proj_frac_a*dif_y;
				
				a.speed = Math.hypot(new_v_x_a, new_v_y_a);
				if (new_v_y_a != 0.0 || new_v_x_a != 0.0)
					a.direction = Math.atan2(new_v_y_a, new_v_x_a);
			}
			
			//velocity b += 2*projection onto dif
			double v_x_b = b.getXVel(t);
			double v_y_b = b.getYVel(t);
			
			double new_v_x_b, new_v_y_b, proj_frac_b;
			proj_frac_b = (v_x_b*dif_x + v_y_b*dif_y)/len_sq_dif;
			if(proj_frac_b > 0.0)
			{
				new_v_x_b = 0.0;//v_x_b - proj_frac_b*dif_x;
				new_v_y_b = 0.0;//v_y_b - proj_frac_b*dif_y;
				
				b.speed = Math.hypot(new_v_x_b, new_v_y_b);
				if (new_v_x_b != 0.0 || new_v_y_b != 0.0)
					b.direction = Math.atan2(new_v_y_b, new_v_x_b);
			}
			return orders;
		}
		return new HashSet<Order>();
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
					setupLogFile("log.txt");
				
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
