package galactic_strategy.user_actions;

import galactic_strategy.Player;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.TimeControl;

public strictfp abstract class Order implements Comparable<Order>
{
	static enum MODE {ORIGIN, NETWORK};
	
	protected MODE mode;
	
	/**Virtual time at which this order should happen*/
	protected long scheduled_time;
	
	/**
	 * Id of the player who created this order
	 */
	protected int p_id;
	
	/**
	 * An integer, given to each order by the player issuing the order.
	 * This should be generated on a per-computer counter.
	 */
	protected int order_number;
	
	public static enum Decision {UNDECIDED, ACCEPT, REJECT};
	Decision decision;
	
	public Order(long time, Player orderer)
	{
		scheduled_time = TimeControl.roundUpToTimeGrain(time);
		p_id = orderer.getId();
		order_number = orderer.getNextOrderNumber();
		decision = Decision.UNDECIDED;
	}
	
	/**This function must perform the following tasks:
	 * 1) retrieve objects from their describers, if the order came over the network
	 * 2) check the validity of the order.
	 * 3) execute the actual Order
	 * 
	 * @return true on success; false if the Order could not be executed.
	 */
	public abstract boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException;

	//remember once an Order is instantiated to ask GameControl to notifyAllPlayers
	
	public Order(){}
	public long getScheduled_time(){return scheduled_time;}
	public void setScheduled_time(long t){scheduled_time=TimeControl.roundUpToTimeGrain(t);}
	public void setP_id(int p){p_id=p;}
	public int getP_id(){return p_id;}
	public void setOrder_number(int n){order_number=n;}
	public int getOrder_number(){return order_number;}
	public Decision getDecision(){return decision;}
	public void setDecision(Decision d){decision = d;}
	
	@Override
	public int compareTo(Order o)
	{
		if (scheduled_time > o.scheduled_time)
			return 1;
		else if(scheduled_time == o.scheduled_time)
		{
			if (p_id > o.p_id)
				return 1;
			else if (p_id == o.p_id)
			{
				if (order_number < o.order_number)
					return -1;
				else if (order_number == o.order_number)
					return 0; //TODO: Is another tie-breaker needed?  I think not.
				else
					return 1;
			}
			else
				return -1;
		}
		else //if(scheduled_time < o.scheduled_time)
			return -1;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Order)
		{
			Order o = (Order) obj;
			if (o.scheduled_time == scheduled_time &&
					o.order_number == order_number &&
					o.p_id == p_id)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return (int) scheduled_time/20 + order_number*100000 + p_id * 100000005;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + "\n"
				+ "\torder number: " + order_number +"\n"
				+ "\tp_id: " + p_id + "\n"
				+ "\tscheduled_time: " + scheduled_time + "\n";
	}
	
	/**the following nested class is for debugging purposes only*/
	public static class OrderDroppedException extends RuntimeException
	{
		private static final long serialVersionUID = 2332552748374034863L;
	}
}