

public strictfp class PlayerDataSaverControl extends RelaxedDataSaverControl<Player, PlayerDataSaver> {

	public PlayerDataSaverControl(Player p) {
		super(p, new Creator<Player, PlayerDataSaver>(){
				@Override
				public PlayerDataSaver create() {return new PlayerDataSaver();}
				@Override
				public PlayerDataSaver[] createArray() {return new PlayerDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}
}
