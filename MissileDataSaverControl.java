
public strictfp class MissileDataSaverControl extends FlyerDataSaverControl<Missile, FlyerDataSaver<Missile>> {

	@SuppressWarnings("unchecked")
	public MissileDataSaverControl(Missile m)
	{
		super(m, new Creator<Missile, FlyerDataSaver<Missile> >(){
				public FlyerDataSaver<Missile> create(){return new FlyerDataSaver<Missile>();}
				public FlyerDataSaver[] createArray(){return new FlyerDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}
}
