
public strictfp class FleetDataSaverControl extends RelaxedDataSaverControl<Fleet, FleetDataSaver> {

	public FleetDataSaverControl(Fleet f) {
		super(f, new Creator<Fleet, FleetDataSaver >(){
			public FleetDataSaver create(){return new FleetDataSaver();}
			public FleetDataSaver[] createArray(){return new FleetDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
