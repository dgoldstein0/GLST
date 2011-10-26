
public strictfp class MissileListDataControl extends RelaxedDataSaverControl<MissileList, MissileListDataSaver> {

	public MissileListDataControl(MissileList m) {
		super(m, new Creator<MissileList, MissileListDataSaver>(){
				public MissileListDataSaver create(){return new MissileListDataSaver();}
				public MissileListDataSaver[] createArray(){return new MissileListDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}
}
