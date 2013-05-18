
public strictfp class ShipyardDataSaverControl extends FacilityDataSaverControl<Shipyard, ShipyardDataSaver> {
	
	public ShipyardDataSaverControl(Shipyard s) {
		super(s, new Creator<Shipyard, ShipyardDataSaver >(){
			public ShipyardDataSaver create(Shipyard syd){return new ShipyardDataSaver();}
			public ShipyardDataSaver[] createArray(){return new ShipyardDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
