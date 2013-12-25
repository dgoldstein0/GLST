package galactic_strategy.testing;

import galactic_strategy.sync_engine.TimeControl;
import galactic_strategy.sync_engine.TimeManager;

public class SimulatedTimeControl implements TimeManager
{
	/**in milliseconds*/
	long cur_time;
	
	public SimulatedTimeControl()
	{
		cur_time = 0;
	}
	
	@Override
	public long getNanoTime() {
		return 1000000*getTime();
	}

	@Override
	public long getNextTimeGrain() {
		return TimeControl.getTimeGrainAfter(getTime());
	}

	@Override
	public long getTime() {
		return cur_time;
	}
	
	public void advanceTime(long t)
	{
		if (t < cur_time)
			throw new RuntimeException("Monotonicity Error");
		cur_time = t;
	}
}