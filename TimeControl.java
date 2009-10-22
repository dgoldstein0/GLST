public class TimeControl
{
	long time_elapsed; //can support over 292 years
	long start_time; 
	
	public TimeControl()
	{
		start_time=System.nanoTime();
		time_elapsed=0;
	}
	
	public long getTime()
	{
		time_elapsed=System.nanoTime()-start_time;
		return time_elapsed/1000000; //convert to milliseconds
	}
	
	public long gettime_elapsed(){return time_elapsed;}
	public void settime_elapsed(long t){time_elapsed=t;}
}