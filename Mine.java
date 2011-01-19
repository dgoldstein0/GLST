
public strictfp class Mine extends Facility<Mine>{
	
	double mining_rate;
	long add_met;
	
	public Mine(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, t, GalacticStrategyConstants.initial_mine_endu);
		double modified_mining_rate = GalacticStrategyConstants.DEFAULT_MINING_RATE;
		for(int j=1;j<location.number_mines;j++){
			modified_mining_rate*=GalacticStrategyConstants.additional_mine_penalty;
		}
		mining_rate=modified_mining_rate;
		data_control = new MineDataSaverControl(this);
		data_control.saveData();
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
	public void updateStatus(long t)
	{
		add_met=0;
		if(t-last_time >= 3000 && location.owner != null) //do nothing unless the location has an owner
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

	@Override
	public void ownerChanged(long t) {
		last_time = t;
		data_control.saveData();
	}
}
