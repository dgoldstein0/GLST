import java.util.ArrayList;
import java.util.List;


public class PlayerDataSaver extends DataSaver<Player> {

	double metal;
	double money;
	List<Saveable<?>> resource_users;
	
	public PlayerDataSaver()
	{
		resource_users = new ArrayList<Saveable<?>>();
	}
	
	@Override
	protected void doLoadData(Player p) {
		synchronized(p.metal_lock)
		{
			synchronized(p.money_lock)
			{
				p.setMoney(money);
				p.setMetal(metal);
				
				p.resource_users.clear();
				p.resource_users.addAll(resource_users);
			}
		}
	}

	@Override
	protected void doSaveData(Player p) {
		synchronized(p.metal_lock)
		{
			synchronized(p.money_lock)
			{
				money = p.getMoney();
				metal = p.getMetal();
				
				resource_users.clear();
				resource_users.addAll(p.resource_users);
			}
		}
	}

}
