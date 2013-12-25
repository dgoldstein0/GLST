package galactic_strategy.sync_engine;

import galactic_strategy.Constants;

public strictfp class TimeControl implements TimeManager
{
	volatile long start_time;
	
	/**
	 * used to gaurantee monotonicity
	 * @GaurdedBy TimeControl object
	 */
	volatile long last_time;
	
	public TimeControl(int offset)
	{
		last_time = 0;
		resetTime(offset);
	}
	
	public void resetTime(int offset)
	{
		start_time = System.nanoTime()-offset;
		last_time = offset;
	}
	
	public static long getTimeGrainAfter(long t)
	{
		long remainder = t % Constants.TIME_GRANULARITY;
		if(remainder != 0)
			return t + Constants.TIME_GRANULARITY - remainder;
		else
			return t;
	}
	
	public static long getTimeGrainBefore(long t) {
		
		long remainder = t % Constants.TIME_GRANULARITY;
		if (remainder != 0)
			return t - remainder;
		else
			return t - Constants.TIME_GRANULARITY;
	}
	
	public static long roundDownToTimeGrain(long t)
	{
		return t - (t % Constants.TIME_GRANULARITY);
	}
	
	public static long roundUpToTimeGrain(long t) {
		if (t % Constants.TIME_GRANULARITY == 0)
			return t;
		else
			return t + Constants.TIME_GRANULARITY - (t % Constants.TIME_GRANULARITY);
	}
	
	public long getNextTimeGrain()
	{
		return getTimeGrainAfter(getTime());
	}
	
	public long getTime()
	{
		long time_elapsed=System.nanoTime()-start_time;
		
		//to guarantee monotonicity
		synchronized(this)
		{
			if (time_elapsed < last_time)
				time_elapsed = last_time;
			last_time = time_elapsed;
		}
		
		return time_elapsed/1000000; //convert to milliseconds - don't need to keep the decimal so no cast to doubles
	}
	
	public long getNanoTime()
	{
		long time_elapsed=System.nanoTime()-start_time;
		
		//to guarantee monotonicity
		synchronized(this)
		{
			if (time_elapsed < last_time)
				time_elapsed = last_time;
			last_time = time_elapsed;
		}
		
		return time_elapsed;
	}
	
	public long getlast_time(){return last_time;}
	public void setlast_time(long t){last_time=t;}

	/**roundUpToNextResourceChange
	 * returns t rounded up to the next multiple of TIME_BETWEEN_RESOURCES.
	 * if t is a multiple of TIME_BETWEEN_RESOURCES, then t is returned.
	 * 
	 * This is used to set the TaxOffice and Mine timing, since we want to make
	 * sure they update on schedule and that they skip their first update,
	 * since this would mean getting resources without putting in the actual time.
	 * */
	public static long roundUpToNextResourceChange(long t) {
		long remainder = t % Constants.TIME_BETWEEN_RESOURCES;
		if(remainder != 0)
			return t + Constants.TIME_BETWEEN_RESOURCES - remainder;
		else
			return t;
	}
}