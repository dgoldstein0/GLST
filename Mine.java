
public class Mine extends Facility{
	
	int mining_rate;
	
	public Mine()
	{
		mining_rate=GalacticStrategyConstants.DEFAULT_MINING_RATE;
		damage=0;
		endurance = GalacticStrategyConstants.initial_mine_endu;
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
}
