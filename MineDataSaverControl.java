
public class MineDataSaverControl extends RelaxedDataSaverControl<Mine> {
	
	public MineDataSaverControl(Mine m) {
		super(m);
		saved_data = new MineDataSaver[GalacticStrategyConstants.data_capacity];
		for(int i=0; i<saved_data.length; i++)
			saved_data[i] = new MineDataSaver();
	}
}
