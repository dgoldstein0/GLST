public abstract class Event implements Runnable
{
	long scheduled_time;
	
	//remember once an event is instantiated to ask GameControl to notifyAllPlayers
}