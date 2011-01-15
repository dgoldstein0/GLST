import java.util.Timer;
import java.util.TimerTask;


public class TaskManager {

	Timer timer;
	TimerTask task;
	
	public TaskManager()
	{
		timer=new java.util.Timer(true);
	}
	
	public void startConstIntervalTask(TimerTask t, int repeatrate)
	{
		stopTask();
		task=t;
		timer.scheduleAtFixedRate(t,0, repeatrate);
	}
	
	public void stopTask()
	{
		if(task != null)
			task.cancel();
	}
	
}
