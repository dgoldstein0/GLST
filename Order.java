import java.util.Set;

public abstract class Order implements Comparable<Order>
{
	final static int ORIGIN = 0;
	final static int NETWORK = 1;
	
	int mode;
	long scheduled_time;
	
	public abstract Set<Order> execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException;
	/**This function must perform the following tasks:
	 * 1) retrieve objects from their describers, if the order came over the network
	 * 2) check the validity of the order.  If not valid, return an empty set.
	 * 3.1) revertToTime on all objects immediately effected (but not those that will be affected by the revertToTime calls)
	 * 3.2) create a set of Order's of everything returned from revertToTime calls.  This will be the return value
	 * 4) execute the actual Order
	 * 5) return the set created in step 3*/
	
	//remember once an Order is instantiated to ask GameControl to notifyAllPlayers
	
	public long getScheduled_time(){return scheduled_time;}
	public void setScheduled_time(long t){scheduled_time=t;}

	/** NOTE: compareTo here is inconsistent with equals*/
	@Override
	public int compareTo(Order o)
	{
		if (scheduled_time > o.scheduled_time)
			return 1;
		else if(scheduled_time == o.scheduled_time)
			return 0;
		else //if(scheduled_time < o.scheduled_time)
			return -1;
	}
}