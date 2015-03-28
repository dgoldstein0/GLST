package galactic_strategy.game_objects;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Describer;
import galactic_strategy.sync_engine.Saveable;

public strictfp class Formation<T extends Formation<T>> implements Saveable<T> {
	private Map<Ship, Integer> ship_to_position;
	private FormationType type;
	private DescribableDestination<?> dest;
	private DataSaverControl<T> data_control;
	
	double x;
	double y;
	double direction;
	
	public enum FormationType {
		LINE(),
		V();
		
		private FormationType(){}
	}
	
	private Formation(){
		@SuppressWarnings("unchecked")
		T typed_this = (T) this;
		data_control = new DataSaverControl<T>(typed_this);
	}
	
	
	// TODO (drg): figure out nice membership transitions.
	
	/**
	 * This function gets called when a ship dies or leaves the formation
	 */
	public void removeShip(Ship s) {
		ship_to_position.remove(s);
	}
	
	public void addShip(Ship s) {
		int least_unoccupied = 0;
		Set<Integer> used_positions = new HashSet<Integer>(ship_to_position.values());
		while (used_positions.contains(least_unoccupied)) {
			least_unoccupied++;
		}
		ship_to_position.put(s, least_unoccupied);
		
		s.setDestination(new VirtualFormationDestination(this, least_unoccupied));
	}
	
	public void disband(){
		ship_to_position.clear();
	}

	@Override
	public DataSaverControl<T> getDataControl() {
		return data_control;
	}
	
	public class VirtualFormationDestination implements AbstractDestination<VirtualFormationDestination> {
		private Formation<?> f;
		private int position;
		
		@Override
		public double getXCoord() {
			return f.getXforShip(position);
		}
		@Override
		public double getYCoord() {
			return f.getYforShip(position);
		}
		@Override
		public double getXVel() {
			return f.getXVelforShip(position);
		}
		@Override
		public double getYVel() {
			return f.getYVelforShip(position);
		}
		@Override
		public String imageLoc() {
			return f.dest.imageLoc();
		}
		@Override
		public String getName() {
			return "Formation";
		}
	}

	public double getXforShip(int position) {
		return 0;
	}

	public double getYforShip(int position) {
		return 0;
	}
	
	public double getYVelforShip(int position) {
		return 0;
	}

	public double getXVelforShip(int position) {
		return 0;
	}
}
