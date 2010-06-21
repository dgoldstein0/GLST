
public class ShipyardDataSaverControl extends RelaxedDataSaverControl<Shipyard> {
	
	public ShipyardDataSaverControl(Shipyard s) {
		super(s);
		saved_data = new ShipyardDataSaver[GalacticStrategyConstants.data_capacity];
		for(int i=0; i<saved_data.length; i++)
			saved_data[i] = new ShipyardDataSaver();
	}
}
