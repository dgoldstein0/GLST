
public class Mine extends Facility<Mine>{
	
	double mining_rate;
	
	public Mine(OwnableSatellite<?> loc, long t)
	{
		super(loc,t, GalacticStrategyConstants.initial_mine_endu);
		mining_rate=GalacticStrategyConstants.DEFAULT_MINING_RATE;
		data_control = new MineDataSaverControl(this);
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
	public void updateStatus(long t)
	{
		long add_met=0;
		while(t-last_time >= 3000 && location.owner instanceof Player) //do nothing unless the location has an owner
		{
			add_met += mining_rate*3000;
			last_time += 3000;
		}
		location.owner.changeMetal(add_met);
		data_control.saveData();
	}
	
	public FacilityType getType(){return FacilityType.MINE;}
	public String imageLoc(){return "images/mine.gif";}
	public String getName(){return "Mine";}
	
	public Mine(){}
	public void setMining_rate(double mr){mining_rate = mr;}
	public double getMining_rate(){return mining_rate;}
}
