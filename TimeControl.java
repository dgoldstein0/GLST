public strictfp class TimeControl implements TimeManager
{
	volatile long time_elapsed; 
	volatile long start_time;
	
	public TimeControl(int offset)
	{
		resetTime(offset);
	}
	
	public void resetTime(int offset)
	{
		start_time=System.nanoTime()-offset;
		time_elapsed=offset;
	}
	
	public static long getTimeGrainAfter(long t)
	{
		long remainder = t % GalacticStrategyConstants.TIME_GRANULARITY;
		if(remainder != 0)
			return t + GalacticStrategyConstants.TIME_GRANULARITY - remainder;
		else
			return t;
	}
	
	public static long roundDownToTimeGrain(long t)
	{
		return t - (t % GalacticStrategyConstants.TIME_GRANULARITY);
	}
	
	public static long roundUpToTimeGrain(long t) {
		if (t % GalacticStrategyConstants.TIME_GRANULARITY == 0)
			return t;
		else
			return t + GalacticStrategyConstants.TIME_GRANULARITY - (t % GalacticStrategyConstants.TIME_GRANULARITY);
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
	
	public long gettime_elapsed(){return time_elapsed;}
	public void settime_elapsed(long t){time_elapsed=t;}

	/**roundUpToNextResourceChange
	 * returns t rounded up to the next multiple of TIME_BETWEEN_RESOURCES.
	 * if t is a multiple of TIME_BETWEEN_RESOURCES, then t is returned.
	 * 
	 * This is used to set the TaxOffice and Mine timing, since we want to make
	 * sure they update on schedule and that they skip their first update,
	 * since this would mean getting resources without putting in the actual time.
	 * */
	public static long roundUpToNextResourceChange(long t) {
		long remainder = t % GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
		if(remainder != 0)
			return t + GalacticStrategyConstants.TIME_BETWEEN_RESOURCES - remainder;
		else
			return t;
	}
}