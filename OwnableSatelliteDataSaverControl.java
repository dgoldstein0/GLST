
public strictfp class OwnableSatelliteDataSaverControl<T extends OwnableSatellite<T>>
 extends DataSaverControl<T, OwnableSatelliteDataSaver<T>>
{
	
	public OwnableSatelliteDataSaverControl(T sat) {
		super(
			sat,
			new Creator<T,  OwnableSatelliteDataSaver<T>>()
			{
				public OwnableSatelliteDataSaver<T> create(T o) {
					return new OwnableSatelliteDataSaver<T>();
				}
				
				@SuppressWarnings("unchecked")
				public OwnableSatelliteDataSaver<T>[] createArray() {
					return new OwnableSatelliteDataSaver[GalacticStrategyConstants.data_capacity];
				}
			}
		);
	}
}
