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
		
		for(int j=getNextIndex(i);j != index; j = getNextIndex(j))
		{
			addAggressorsAtIndex(j, effects.objects_to_revert);
		}
		
		return effects;
	}
	protected void doReversionPrep(int indx)
	{
		for(int i = getNextIndex(indx); i != index; i++)
		{
			the_obj.location.data_control.saved_data[the_obj.location.data_control.getIndexForTime(saved_data[i].t)].own.changeMoney(-saved_data[i].money_added);
		}
	}
}
