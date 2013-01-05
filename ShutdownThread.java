
public class ShutdownThread extends Thread {

	final GameControl GC;
	
	public ShutdownThread(GameControl gc)
	{
		GC = gc;
	}
	
	public void run()
	{
		GC.endAllThreads();
	}
}
