
public class Mine extends Facility{
	
	double mining_rate;
	
	public Mine(OwnableSatellite loc, long t)
	{
		super(loc,t, GalacticStrategyConstants.initial_mine_endu);
		mining_rate=GalacticStrategyConstants.DEFAULT_MINING_RATE;
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
	public void updateStatus(long t)
	{
		if(t-last_time >= 3000 && location.owner instanceof Player) //do nothing unless the location has an owner
		{
			location.owner.changeMetal(mining_rate*(t-last_time));
			last_time = t;
		}
	}
	
	public int getType(){return Facility.MINE;}
	public String imageLoc(){return "images/mine.gif";}
	public String getName(){return "Mine";}
	
	public Mine(){}
	public void setMining_rate(double mr){mining_rate = mr;}
	public double getMining_rate(){return mining_rate;}
	public void setLast_time(long t){last_time=t;}
	public long getLast_time(){return last_time;}
}
