public strictfp class ShipAttackOrder extends Order
{
	Ship the_ship;
	Targetable<?> the_target;
	
	ShipDescriber ship_desc;
	Describer<? extends Targetable<?>> tgt_desc;
	long target_t;
	
	public ShipAttackOrder(Player p, Ship s, long t, long target_time, Targetable<?> tgt)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		
		the_ship = s;
		ship_desc=new ShipDescriber(p,s);
		
		the_target = tgt;
		tgt_desc=tgt.describer();
		
		target_t=target_time;
	}
	
	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		the_ship = ship_desc.retrieveObject(g, scheduled_time);
		the_target = tgt_desc.retrieveObject(g, target_t);
		
		//if we couldn't find the ship, the target, or the ship is not alive at scheduled time, order is moot
		if(the_ship != null && the_ship.isAlive() && the_target != null)
		{
			//check if the target is alive at scheduled time.  if not, then target was destroyed (assuming you can't order attacks on dead ships, in which case the target never should have been targeted, but we'll ignore that possibility)
			if(the_target.isAlive())
			{
				if(!(the_target instanceof Ship))
				{
					the_ship.orderToAttack(scheduled_time, the_target);
				}
				else
				{
					if(((Ship)the_target).location == the_ship.location)
					{
						the_ship.orderToAttack(scheduled_time, the_target);
					}
					else
					{
						the_ship.targetHasWarped(scheduled_time, true, the_target);
					}
				}
			}
			else
			{
				the_ship.targetIsDestroyed(scheduled_time, true, the_target);
			}
			
			decision = Decision.ACCEPT;
			return true;
		}
		else
		{
			decision = Decision.REJECT;
			return false;
		}
	}
	
	public ShipAttackOrder(){mode=Order.MODE.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public Describer<? extends Targetable<?>> getTgt_desc(){return tgt_desc;}
	public void setTgt_desc(Describer<? extends Targetable<?>> t){tgt_desc=t;}
	public long getTarget_t(){return target_t;}
	public void setTarget_t(long t){target_t=t;}
}