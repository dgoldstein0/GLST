
public class OwnableSatelliteDataSaverControl extends RelaxedDataSaverControl<OwnableSatellite<?>> {
	public OwnableSatelliteDataSaverControl(OwnableSatellite<?> sat) {
		super(sat);
		saved_data = new OwnableSatelliteDataSaver[GalacticStrategyConstants.data_capacity];
		for(int i=0; i<saved_data.length; i++)
			saved_data[i] = new OwnableSatelliteDataSaver();
	}
}
