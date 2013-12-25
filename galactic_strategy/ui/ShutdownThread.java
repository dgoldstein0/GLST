package galactic_strategy.ui;
import galactic_strategy.GameControl;

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
