public class ShipAttackMoveOrder extends Order {

	Ship the_ship;
	Destination<?> the_dest;
	
	Describer<Ship> ship_desc;
	Describer<? extends Destination<?>> dest_desc;

	public ShipAttackMoveOrder (Player p, Ship s, long t,Destination<?> d)
	{
		super(t, p);
		mode = Order.MODE.ORIGIN;
		
		the_ship = s;
		ship_desc=s.describer();
		
		the_dest = d;
		dest_desc=d.describer();
		
		scheduled_time=t;
	}

	@Override
	public boolean execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException
	{
		{
			the_ship = ship_desc.retrieveObject(g);
			the_dest = dest_desc.retrieveObject(g);
			
			/*System.out.println("ship move order executing with scheduled_time = " + Long.toString(scheduled_time));
			System.out.println("\tthe_ship is null: " + Boolean.toString(the_ship == null));
			System.out.println("\tthe_dest is null: " + Boolean.toString(the_dest==null));
			if(the_ship != null)
			{
				System.out.println("\tthe_ship is alive at scheduled_time: " + Boolean.toString(the_ship.isAliveAt(scheduled_time)));
				System.out.println("\tthe_ship.owner.getId() = " + Integer.toString(the_ship.owner.getId()) + " and player_id = " + Integer.toString(player_id));
			}*/
			
			if(the_ship != null && the_dest != null && the_ship.isAlive()
					&& the_ship.owner.getId() == p_id)
			{
				the_ship.orderToAttackMove(the_dest);
				decision = Decision.ACCEPT;
				return true;
			}
			else
			{
				decision = Decision.REJECT;
				return false;
			}
		}
	}
	public ShipAttackMoveOrder(){mode=Order.MODE.NETWORK;}
	public Describer<Ship> getShip_desc(){return ship_desc;}
	public void setShip_desc(Describer<Ship> sd){ship_desc=sd;}
	public Describer<? extends Destination<?>> getDest_desc(){return dest_desc;}
	public void setDest_desc(Describer<? extends Destination<?>> d){dest_desc=d;}
}
