import java.util.Set;
import java.util.HashSet;

public class ShipAttackOrder extends Order
{
	Ship the_ship;
	Targetable<?> the_target;
	
	ShipDescriber ship_desc;
	Describer<? extends Targetable<?>> tgt_desc;
	
	public ShipAttackOrder(Player p, Ship s, long t, Targetable<?> tgt)
	{
		mode = Order.ORIGIN;
		
		the_ship = s;
		ship_desc=new ShipDescriber(p,s);
		
		the_target = tgt;
		tgt_desc=tgt.describer();
		
		scheduled_time=t;
	}
	
	public Set<Order> execute(Galaxy g)
	{
		if(mode==Order.NETWORK)
		{
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
			the_target = tgt_desc.retrieveObject(g, scheduled_time);
		}
		
		if(the_ship != null && the_target != null && the_ship.isAliveAt(scheduled_time) && the_target.isAliveAt(scheduled_time))
		{
			Set<Order> orders = the_ship.data_control.revertToTime(scheduled_time);
			orders.addAll(the_target.getDataControl().revertToTime(scheduled_time));
			
			the_ship.orderToAttack(scheduled_time, the_target);
			
			return orders;
		}
		
		return new HashSet<Order>();
	}
	
	public ShipAttackOrder(){mode=Order.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<? extends Targetable<?>> getTgt_desc(){return tgt_desc;}
	public void setTgt_desc(Describer<? extends Targetable<?>> t){tgt_desc=t;}
}