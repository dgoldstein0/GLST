
public strictfp class Mine extends Facility<Mine>{
	
	long last_resource_time;
	
	public Mine(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, GalacticStrategyConstants.initial_mine_endu);
		location.number_mines++;
		
		//set time to the next resource change, and save.  Need to align ourselves to the
		//timing of resource updates, but can't do it via super() call because then our first
		//record doesn't correspond to the time the Mine was built
		last_resource_time = TimeControl.roundUpToNextResourceChange(t);
	}
	
	@Override
	public void destroyed(long t)
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
			location.number_mines--;
		}
	}
	
	@Override
	public void removeFromGame()
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
		if(t-last_resource_time >= GalacticStrategyConstants.TIME_BETWEEN_RESOURCES && location.owner != null) //do nothing unless the location has an owner
		{
			add_met += location.calcMiningrate()*GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
			last_resource_time += GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
		}
		location.owner.changeMetal(add_met);
	}
	
	public FacilityType getType(){return FacilityType.MINE;}
	
	public Mine(){}

	@Override
	public void ownerChanged(long t) {
		last_resource_time = TimeControl.roundUpToNextResourceChange(t);
	}
	
	public long getLast_resource_time(){return last_resource_time;}
	public void setLast_resource_time(long l){last_resource_time=l;}
}
