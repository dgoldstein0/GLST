
public class ShipDataSaverControl extends FlyerDataSaverControl<Ship> {

	public ShipDataSaverControl(Ship s)
	{
		super(s);
		saved_data = new ShipDataSaver[GalacticStrategyConstants.data_capacity];
		for(int i=0; i < GalacticStrategyConstants.data_capacity; i++)
			saved_data[i] = new ShipDataSaver();
	}
}
