import java.beans.ExceptionListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;
import javax.swing.SwingUtilities;

public class GameUpdater {

	static final boolean DEBUGGING = false;
	
	TimeManager TC;
	final GameControl GC;
	final PriorityBlockingQueue<Order> pending_execution;
	TaskManager TM;
	long last_time_updated;
	
	public GameUpdater(GameControl ctrl)
	{
		pending_execution = new PriorityBlockingQueue<Order>();
		TM = new TaskManager();
		GC = ctrl;
		last_time_updated = 0l;
	}
	
	public void setTimeManager(TimeManager TM)
	{
		TC=TM;
	}
	
	public void startUpdating()
	{
		TM.startConstIntervalTask(new Updater(),(int)GalacticStrategyConstants.TIME_GRANULARITY);
	}
	
	public void stopUpdating()
	{
		TM.stopTask();
		
		//TODO: debugging code, should later be removed
		try {
			if(logFile != null)
				logFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		log("updateGame with time_elapsed=" + time_elapsed,null);
		
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
						though with a bit of reversion*/
		} while(true);
		
		/*now figure out the earliest order in local_pending_execution, and make that time update_to,
		 * if update_to is currently larger*/
		Order first_order = local_pending_execution.peek();
		if(first_order != null)
		{
			long next_order_time = first_order.scheduled_time;
			if(next_order_time < update_to)
				update_to = next_order_time; //forces the main loop to reconsider below
		}
		
		
		//update data in all systems
		for(GSystem sys : GC.map.systems)
		{
			//move all planets
			for(Satellite<?> sat : sys.orbiting)
			{
				if(sat instanceof Planet)
				{
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
			Order o;
			while( (o = local_pending_execution.peek()) != null && o.scheduled_time <= update_to)
			{
				/**execute does all the necessary reversion itself.  It never reverts anything to earlier than
				 * scheduled_time, which should be within one time grain less than update_to*/
				local_pending_execution.addAll(local_pending_execution.remove().execute(GC.map));
			}
			
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
			}
			
			//TODO: debuging code, should later be removed
			if(DEBUGGING)
			{
				log("\r\nUpdated to " + update_to, GC.map);
				log("\n",GC.players);
			}
		}
		
		setLast_time_updated(update_to);
		
		//This can flare up in the case that time_elapsed >= order time, but the last time grain
		//run here happens to be before order time since both don't have to land on a time grain
		//(at least, for some orders).  I.e. time_elapsed=56, order @ 53, will only update through
		// time 40 (time_grain is 20)
		if(!local_pending_execution.isEmpty())
		{
			System.out.println("We still have orders in the local queue.  should not be possible!");
			pending_execution.addAll(local_pending_execution);
		}
		
		SwingUtilities.invokeLater(new InterfaceUpdater(time_elapsed));
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
	
	BufferedOutputStream logFile;
	
	public void setupLogFile()
	{
		try {
			logFile = new BufferedOutputStream(new FileOutputStream("log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(String message, Object o)
	{
		if(logFile == null)
			setupLogFile();
		
		try {
			logFile.write(("\n"+message).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(o != null)
		{
			XMLEncoder2 encoder = new XMLEncoder2(logFile);
			encoder.setExceptionListener(new MyExceptionListener());
			encoder.writeObject(o);
			encoder.finish();
		}
	}
	
	public static class MyExceptionListener implements ExceptionListener
	{
		@Override
		public void exceptionThrown(Exception arg0) {
			// TODO Auto-generated method stub
			arg0.printStackTrace();
		}
	}
	
	public long getLast_time_updated(){return last_time_updated;}
	public void setLast_time_updated(long t){last_time_updated=t;}
}
