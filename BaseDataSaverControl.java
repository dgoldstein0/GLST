public strictfp class BaseDataSaverControl extends FacilityDataSaverControl<Base, BaseDataSaver> {
	
	public BaseDataSaverControl(Base b) {
		super(b, new Creator<Base, BaseDataSaver >(){
			public BaseDataSaver create(){return new BaseDataSaver();}
			public BaseDataSaver[] createArray(){return new BaseDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
