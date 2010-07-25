import java.util.HashMap;


public class FleetDataSaver extends DataSaver<Fleet> {

	HashMap<Ship.ShipId, Ship> ships;
	
	@Override
	protected void doLoadData(Fleet f) {
		
		synchronized(f.lock)
		{
			/**TODO: (double check this) line isn't necessary here
			 * because ships cannot be modified from two different
			 * threads - only modified by updateGame and ONLY read
			 * in other places
			 * 
			 * also not necessary because of setTime in DataSaver.loadData*/
			//f.last_time_changed = t;
			
			f.ships = (HashMap<Ship.ShipId, Ship>)ships.clone();
		}
	}

	@Override
	protected void doSaveData(Fleet f) {
		
		ships = (HashMap<Ship.ShipId, Ship>)f.ships.clone();
	}

}
