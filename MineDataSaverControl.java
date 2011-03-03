public strictfp class MineDataSaverControl extends FacilityDataSaverControl<Mine, MineDataSaver> {
	
	public MineDataSaverControl(Mine m) {
		super(m, new Creator<Mine, MineDataSaver >(){
			public MineDataSaver create(){return new MineDataSaver();}
			public MineDataSaver[] createArray(){return new MineDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}

	//just gathers up aggressors
	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		
		ReversionEffects effects = new ReversionEffects();
		
		//revert whole planet - in case the number of mines changed
		effects.objects_to_revert.add(new ReversionEffects.RevertObj(the_obj.location, saved_data[indx].t));
		
		//revert Player too
		effects.objects_to_revert.add(new ReversionEffects.RevertObj(the_obj.location.owner, saved_data[indx].t));
		
		//revert all other mines too
		/*OwnableSatelliteDataSaverControl<?> loc_ctrl = the_obj.location.data_control;
		HashMap<Integer, Facility<?>> facilities = loc_ctrl.saved_data[loc_ctrl.getIndexForTime(saved_data[indx].t)].fac;
		for(Integer i : facilities.keySet())
		{
			Facility<?> f = facilities.get(i);
			if(f.getType() == FacilityType.MINE)
			{
				effects.objects_to_revert.add(new ReversionEffects.RevertObj(f, saved_data[indx].t));
			}
		}*/
		
		for(int j=getNextIndex(indx); j != index; j=getNextIndex(j))
		{
			addAggressorsAtIndex(j, effects.objects_to_revert);
		}
		
		return effects;
	}
}
