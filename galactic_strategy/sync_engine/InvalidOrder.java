package galactic_strategy.sync_engine;

import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.user_actions.Order;

public class InvalidOrder extends Order {

	public InvalidOrder(int p_id, long scheduled_time, int order_number) {
		this.p_id = p_id;
		this.scheduled_time = scheduled_time;
		this.order_number = order_number;
	}
	
	@Override @Deprecated
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Executing an InvalidOrder == BAD!");
	}

}
