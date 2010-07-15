import java.util.HashMap;


public class FleetDataSaver extends DataSaver<Fleet> {

	HashMap<Ship.ShipId, Ship> ships;
	
	@Override
	public void loadData(Fleet f) {
		
		synchronized(f.lock)
		{
			f.ships = (HashMap<Ship.ShipId, Ship>)ships.clone();
		}
	}

	@Override
	protected void doSaveData(Fleet f) {
		
		ships = (HashMap<Ship.ShipId, Ship>)f.ships.clone();
	}

}
