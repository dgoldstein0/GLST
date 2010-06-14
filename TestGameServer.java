public class TestGameServer
{
	public static void main(String[] args)
	{
		GameControl GC = new GameControl();
		GC.host();
		try
		{
			GC.serverThread.join();
		}
		catch(InterruptedException IE){return;}
		
		GC.startGameViaThread();
	}
}