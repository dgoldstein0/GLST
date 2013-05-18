public strictfp class MineDataSaverControl extends FacilityDataSaverControl<Mine, MineDataSaver> {
	
	public MineDataSaverControl(Mine m) {
		super(m, new Creator<Mine, MineDataSaver >(){
			public MineDataSaver create(Mine m){return new MineDataSaver();}
			public MineDataSaver[] createArray(){return new MineDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
