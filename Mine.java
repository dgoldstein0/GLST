
public strictfp class Mine extends Facility<Mine>{
	
	
	public Mine(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, t, GalacticStrategyConstants.initial_mine_endu);
		location.number_mines++;
		data_control = new MineDataSaverControl(this);
		
		//set time to the next resource change, and save.  Need to align ourselves to the
		//timing of resource updates, but can't do it via super() call because then our first
		//record doesn't correspond to the time the Mine was built
		last_time = TimeControl.roundUpToNextResourceChange(t);
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
		long add_met=0;
		if(t-last_time >= GalacticStrategyConstants.TIME_BETWEEN_RESOURCES && location.owner != null) //do nothing unless the location has an owner
		{
			add_met += location.calcMiningrate()*GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
			last_time += GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
		}
		location.owner.changeMetal(add_met, last_time);
	}
	
	public FacilityType getType(){return FacilityType.MINE;}
	
	public Mine(){}

	@Override
	public void ownerChanged(long t) {
		last_time = t;
	}
}
