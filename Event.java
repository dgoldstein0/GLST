public abstract class Event
{
	long scheduled_time;
	
	//remember once an event is instantiated to ask GameControl to notifyAllPlayers
	
	public void execute()
	{
		//override this method to make an event happen
	}
}