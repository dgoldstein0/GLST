import java.util.ArrayList;

public class PlayerDataSaver extends DataSaver<Player> {

	double metal;
	double money;
	ArrayList<Ship> in_transit;
	
	//TODO: should known_systems or known_satellites be saved?
	
	public PlayerDataSaver()
	{
		in_transit = new ArrayList<Ship>();
	}
	
	@Override
	protected void doLoadData(Player p) {
		synchronized(p.metal_lock)
		{
			synchronized(p.money_lock)
			{
				p.setMoney(money);
				p.setMetal(metal);
			}
		}
		
		p.ships_in_transit.clear();
		p.ships_in_transit.addAll(in_transit);
	}

	@Override
	protected void doSaveData(Player p) {
		synchronized(p.metal_lock)
		{
			synchronized(p.money_lock)
			{
				money = p.getMoney();
				metal = p.getMetal();
			}
		}
		
		in_transit.clear();
		in_transit.addAll(p.ships_in_transit);
	}

}
