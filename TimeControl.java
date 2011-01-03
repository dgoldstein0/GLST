import java.util.*;

public strictfp class TimeControl
{
	long time_elapsed; //can support over 292 years
	long last_time_updated;
	long start_time;
	Timer timer;
	TimerTask task;
	
	public TimeControl(int offset)
	{
		resetTime(offset);
		timer=new java.util.Timer(true);
		last_time_updated = 0l;
	}
	
	public void resetTime(int offset)
	{
		start_time=System.nanoTime()-offset;
		time_elapsed=offset;
	}
	
	public long getTimeGrainAfter(long t)
	{
		return (long)(Math.ceil((double)(t)/(double)(GalacticStrategyConstants.TIME_GRANULARITY))*GalacticStrategyConstants.TIME_GRANULARITY);
	}
	
	public long getNextTimeGrain()
	{
		return getTimeGrainAfter(getTime());
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
	public long getLast_time_updated(){return last_time_updated;}
	public void setLast_time_updated(long t){last_time_updated=t;}
}