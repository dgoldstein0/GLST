public class ShipAttackOrder extends Order
{
	Ship the_ship;
	Targetable<?> the_target;
	
	ShipDescriber ship_desc;
	Describer<? extends Targetable<?>> tgt_desc;
	
	public ShipAttackOrder(Player p, Ship s, long t, Targetable<?> tgt)
	{
		mode = Order.ORIGIN;
		ship_desc=new ShipDescriber(p,s);
		tgt_desc=tgt.describer();
		scheduled_time=t;
	}
	
	public void execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g);
			the_target = tgt_desc.retrieveObject(g);
		}
		
		the_ship.orderToAttack(scheduled_time, the_target);
	}
	
	public ShipAttackOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<? extends Targetable<?>> getTgt_desc(){return tgt_desc;}
	public void setTgt_desc(Describer<? extends Targetable<?>> t){tgt_desc=t;}
}