import java.util.*;

public class TimeControl
{
	long time_elapsed; //can support over 292 years
	long start_time;
	Timer timer;
	TimerTask task;
	
	public TimeControl(int offset)
	{
		resetTime(offset);
		timer=new java.util.Timer(true);
	}
	
	public void resetTime(int offset)
	{
		start_time=System.nanoTime()-offset;
		time_elapsed=offset;
	}
	
	public long getTime()
	{
		time_elapsed=System.nanoTime()-start_time;
		return time_elapsed/1000000; //convert to milliseconds - don't need to keep the decimal so no cast to doubles
	}
	
	public long getNanoTime()
	{
		time_elapsed=System.nanoTime()-start_time;
		return time_elapsed;
	}
	
	public void startConstIntervalTask(TimerTask t, int repeatrate)
	{
		stopTask();
		task=t;
		timer.scheduleAtFixedRate(t,0, repeatrate);
	}
	
	public void stopTask()
	{
		if(task instanceof TimerTask)
			task.cancel();
	}
	
	public long gettime_elapsed(){return time_elapsed;}
	public void settime_elapsed(long t){time_elapsed=t;}
}