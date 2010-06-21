
public class MissileDataSaverControl extends FlyerDataSaverControl<Missile> {

	@SuppressWarnings("unchecked")
	public MissileDataSaverControl(Missile m)
	{
		super(m);
		saved_data = new FlyerDataSaver[GalacticStrategyConstants.data_capacity]; //unchecked cast - FlyerDataSaver[] to FlyerDataSaver<Missile>[]
		for(int i=0; i<GalacticStrategyConstants.data_capacity; i++)
			saved_data[i] = new FlyerDataSaver<Missile>();
	}
}
