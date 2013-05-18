
public strictfp class MissileDataSaverControl extends FlyerDataSaverControl<Missile, MissileDataSaver> {

	public MissileDataSaverControl(Missile m)
	{
		super(m, new Creator<Missile, MissileDataSaver>(){
				public MissileDataSaver create(Missile m){return new MissileDataSaver();}
				public MissileDataSaver[] createArray(){return new MissileDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}
}
