
public strictfp class ShipInvadeOrder extends Order {

	Ship the_ship;
	ShipDescriber ship_desc;
	
	public ShipInvadeOrder(Player p, Ship s, long t)
	{
		super(t, p);
		
		mode = Order.MODE.ORIGIN;
		the_ship = s;
		ship_desc=new ShipDescriber(p,s);
	}
	
	@Override
	public void execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		
		if(mode == Order.MODE.NETWORK)
			the_ship = ship_desc.retrieveObject(g, scheduled_time);
		
		//validate order
		if(the_ship != null)
		{
			the_ship.update(scheduled_time, null); //update by one increment - this will do nothing if we are already past up to date/in need of reversion
			ShipDataSaver data = (ShipDataSaver)the_ship.data_control.saved_data[the_ship.data_control.getIndexForTime(scheduled_time)];
			
			if(the_ship.isAliveAt(scheduled_time) && data.dest instanceof OwnableSatellite<?>
				&& the_ship.owner.getId() == p_id)
			{				
				the_ship.orderToInvade((OwnableSatellite<?>)the_ship.destination,scheduled_time);
			}
			else
				orderDropped();
		}
		else
			orderDropped();
	}

	public ShipInvadeOrder(){mode=Order.MODE.NETWORK;}
	public ShipDescriber getShip_desc(){return ship_desc;}
	public void setShip_desc(ShipDescriber sd){ship_desc=sd;}
	public int getPlayer_id(){return p_id;}
	public void setPlayer_id(int i){p_id = i;}
}
