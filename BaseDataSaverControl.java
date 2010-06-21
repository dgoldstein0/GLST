
public class BaseDataSaverControl extends RelaxedDataSaverControl<Base> {
	
	public BaseDataSaverControl(Base b) {
		super(b);
		saved_data = new BaseDataSaver[GalacticStrategyConstants.data_capacity];
		for(int i=0; i<saved_data.length; i++)
			saved_data[i] = new BaseDataSaver();
	}
}
