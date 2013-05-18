
public strictfp class ShipDataSaverControl extends FlyerDataSaverControl<Ship, ShipDataSaver> {

	public ShipDataSaverControl(Ship s)
	{
		super(s, new Creator<Ship, ShipDataSaver>(){
			public ShipDataSaver create(Ship s){return new ShipDataSaver();}
			public ShipDataSaver[] createArray(){return new ShipDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
}
