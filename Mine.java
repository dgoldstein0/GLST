
public class Mine extends Facility{
	
	double mining_rate;
	long last_time;
	
	public Mine(OwnableSatellite loc, long t)
	{
		mining_rate=GalacticStrategyConstants.DEFAULT_MINING_RATE;
		damage=0;
		endurance = GalacticStrategyConstants.initial_mine_endu;
		last_time = t;
		location=loc;
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
	public void updateStatus(long t)
	{
		if(t-last_time >= 3000)
		{
			location.owner.changeMetal(mining_rate*(t-last_time));
			last_time = t;
		}
	}
	
	public int getType(){return Facility.MINE;}
	public String getImgLoc(){return "images/mine.gif";}
	public String getName(){return "Mine";}
}
