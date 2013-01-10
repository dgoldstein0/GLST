
public strictfp class OwnableSatelliteDataSaverControl<T extends OwnableSatellite<T>>
 extends DataSaverControl<T, OwnableSatelliteDataSaver<T>>
{
	
	public OwnableSatelliteDataSaverControl(T sat) {
		super(
			sat,
			new Creator<T,  OwnableSatelliteDataSaver<T>>()
			{
				public OwnableSatelliteDataSaver<T> create() {
					return new OwnableSatelliteDataSaver<T>();
				}
				
				public OwnableSatelliteDataSaver<T>[] createArray() {
					return new OwnableSatelliteDataSaver[GalacticStrategyConstants.data_capacity];
				}
			}
		);
	}
}
