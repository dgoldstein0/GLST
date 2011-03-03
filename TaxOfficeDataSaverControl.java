public class TaxOfficeDataSaverControl extends
		FacilityDataSaverControl<TaxOffice, TaxOfficeDataSaver> {
	
	public TaxOfficeDataSaverControl(TaxOffice t){
		super(t, new Creator<TaxOffice, TaxOfficeDataSaver >(){
			public TaxOfficeDataSaver create() {return new TaxOfficeDataSaver();}
			public TaxOfficeDataSaver[] createArray(){return new TaxOfficeDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
	
	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int i) {
		
		ReversionEffects effects = new ReversionEffects();
		
		//revert whole planet - in case # of tax offices changed
		effects.objects_to_revert.add(new ReversionEffects.RevertObj(the_obj.location, saved_data[i].t));
		
		//revert Player to adjust to correct amount of money
		effects.objects_to_revert.add(new ReversionEffects.RevertObj(the_obj.location.owner, saved_data[i].t));
		
		for(int j=getNextIndex(i);j != index; j = getNextIndex(j))
		{
			addAggressorsAtIndex(j, effects.objects_to_revert);
		}
		
		return effects;
	}
}
