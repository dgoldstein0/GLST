public class TaxOfficeDataSaverControl extends
		FacilityDataSaverControl<TaxOffice, TaxOfficeDataSaver> {
	
	public TaxOfficeDataSaverControl(TaxOffice t){
		super(t, new Creator<TaxOffice, TaxOfficeDataSaver >(){
			public TaxOfficeDataSaver create() {return new TaxOfficeDataSaver();}
			public TaxOfficeDataSaver[] createArray(){return new TaxOfficeDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
