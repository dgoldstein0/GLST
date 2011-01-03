public strictfp class BaseDataSaverControl extends FacilityDataSaverControl<Base, BaseDataSaver> {
	
	public BaseDataSaverControl(Base b) {
		super(b, new Creator<Base, BaseDataSaver >(){
			public BaseDataSaver create(){return new BaseDataSaver();}
			public BaseDataSaver[] createArray(){return new BaseDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
	
	//just gathers up aggressors AND troopTakers
	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx) {
		
		ReversionEffects effects = new ReversionEffects();
		
		for(int j=getNextIndex(indx); j != index; j=getNextIndex(j))
		{
			//add the aggressors
			addAggressorsAtIndex(j, effects.objects_to_revert);
			
			//add the troop takers
			for(Saveable<?> troop_taker : saved_data[j].taker)
			{
				if(!effects.objects_to_revert.contains(troop_taker));
					effects.objects_to_revert.add(new ReversionEffects.RevertObj(troop_taker, saved_data[j].t));
			}
		}
		
		return effects;
	}
}
