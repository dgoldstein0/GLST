public abstract class Order
{
	long scheduled_time;
	
	//remember once an event is instantiated to ask GameControl to notifyAllPlayers
	
	public long getScheduled_time(){return scheduled_time;}
	public void setScheduled_time(long t){scheduled_time=t;}
}