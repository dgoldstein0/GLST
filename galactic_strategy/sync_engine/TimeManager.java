package galactic_strategy.sync_engine;

public interface TimeManager {

	public long getNextTimeGrain();
	public long getTime();
	public long getNanoTime();
}
