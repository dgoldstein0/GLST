

public strictfp class PlayerDataSaverControl extends RelaxedDataSaverControl<Player, PlayerDataSaver> {

	public PlayerDataSaverControl(Player p) {
		super(p, new Creator<Player, PlayerDataSaver>(){
				@Override
				public PlayerDataSaver create() {return new PlayerDataSaver();}
				@Override
				public PlayerDataSaver[] createArray() {return new PlayerDataSaver[GalacticStrategyConstants.data_capacity];}
			});
	}

	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int i) {
		
		ReversionEffects effected = new ReversionEffects();
		
		for(int indx = getNextIndex(i); indx != index; indx=getNextIndex(indx))
		{
			for(Saveable<?> obj : saved_data[indx].resource_users)
				effected.objects_to_revert.add(new ReversionEffects.RevertObj(obj, saved_data[indx].t));
		}
		
		return effected;
	}

}
