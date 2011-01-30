
public strictfp class Mine extends Facility<Mine>{
	
	double mining_rate;
	long add_met;
	int last_known_mine_amount;
	
	public Mine(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, t, GalacticStrategyConstants.initial_mine_endu);
		location.number_mines++;
		last_known_mine_amount=-1;
		mining_rate = GalacticStrategyConstants.DEFAULT_MINING_RATE;
		data_control = new MineDataSaverControl(this);
		data_control.saveData();
	}

	public void setMiningrate(int r)             //possibly upgrade mining speed through research
	{
		mining_rate=r;
	}
	
	public double calcMiningrate(){
		double modified_mining_rate = GalacticStrategyConstants.DEFAULT_MINING_RATE;
		for(int j=1;j<location.number_mines;j++){
			modified_mining_rate*=GalacticStrategyConstants.additional_mine_penalty;
		}
		return modified_mining_rate;
	}
	
	@Override
	public void destroyed()
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
			location.number_mines--;
		}
	}
	
	@Override
	public void removeFromGame(long t)
	{
		synchronized(location.facilities)
		{
			location.facilities.remove(id);
			location.number_mines--;
		}
	}
	@Override
	public void updateStatus(long t)
	{
		if(location.number_mines!=last_known_mine_amount)
		{
			last_known_mine_amount=location.number_mines;
			mining_rate = calcMiningrate();
			
		}
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
