public abstract class Order
{
	final static int ORIGIN = 0;
	final static int NETWORK = 1;
	
	int mode;
	long scheduled_time;
	
	public abstract void execute(Galaxy g);
	//remember once an event is instantiated to ask GameControl to notifyAllPlayers
	
	public long getScheduled_time(){return scheduled_time;}
	public void setScheduled_time(long t){scheduled_time=t;}
}