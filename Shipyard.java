
import java.util.*;

public class Shipyard extends Facility<Shipyard>{

	Hashtable<Integer, Ship> manufac_queue;      //manufacturing queue - the list of ships to build
	Object queue_lock = new Object(); //used to synchronize the queue
	int next_queue_id;
	
	//TODO: add getters and setters, make an order to change this, add that order into the deduceEffected in ShipyardDataSaverControl
	double assemble_x;  //x coord of assemble point
	double assemble_y;	//y coord
	
	//right now, these coords are not saved.
	double default_x = 0;	//default coords to create the new ship, then order it to move to assemble point
	double default_y = 0;
	
	long time_on_current_ship;
	
	public Shipyard(OwnableSatellite<?> loc, int i, long t) {
		super(loc, i, t, GalacticStrategyConstants.initial_shipyard_endu);
		manufac_queue=new Hashtable<Integer,Ship>(GalacticStrategyConstants.queue_capa);
		time_on_current_ship = 0;
		data_control = new ShipyardDataSaverControl(this);
		next_queue_id=0;
		data_control.saveData();
	}
	
	//for saving/loading data
	public Shipyard(){}
	public Hashtable<Integer, Ship> getManufac_queue(){return manufac_queue;}
	public void setManufac_queue(Hashtable<Integer, Ship> q){manufac_queue = q;}
	public int getNext_queue_id(){return next_queue_id;}
	public void setNext_queue_id(int i){next_queue_id = i;}
	public long getTime_on_current_ship(){return time_on_current_ship;}
	public void setTime_on_current_ship(long t){time_on_current_ship=t;}
	
	public boolean addToQueue(Ship ship, long t, boolean notify)
	{
		boolean ret;
		int met = ship.type.metal_cost;
		int mon = ship.type.money_cost;
		
		synchronized(location.owner.metal_lock){
			synchronized(location.owner.money_lock){
				if(location.owner.metal >= met && location.owner.money >= mon)
				{
					location.owner.metal -= met;
					location.owner.money -= mon;
					
					ship.id = new Ship.ShipId(next_queue_id, this);
					synchronized(queue_lock)
					{
						manufac_queue.put(next_queue_id++,ship);
						if(manufac_queue.size() == 1) //the ship must start production immediately.  This marks the start time.
							last_time=t;
					}
					data_control.saveData();
					if(notify)
						GameInterface.GC.notifyAllPlayers(new ShipyardBuildShipOrder(this, ship.type, t));
					ret=true;
				}
				else
					ret=false;
			}
		}
		return ret;
	}
	
	public void removeFromQueue(Ship ship, long t, boolean notify)
	{
		synchronized(queue_lock)
		{
			manufac_queue.remove(ship.getId().queue_id);
			
			if(manufac_queue.size() == 0)
			{
				time_on_current_ship=0l;
			}
			else
			{
				//refund money and metal
				location.owner.changeMoney(ship.type.money_cost);
				location.owner.changeMetal(ship.type.metal_cost);
			}
			data_control.saveData();
			
			if(notify)
				GameInterface.GC.notifyAllPlayers(new ShipyardCancelBuildOrder(this, ship, t));
			
			return;
		}
	}
		
	public void produce(long t)
	{
		Ship newship;
		synchronized(queue_lock)
		{
			int first = Collections.min(manufac_queue.keySet());
			
			newship=manufac_queue.get(first);//produce the 1st one in the queue
			manufac_queue.remove(first);
		}
		newship.assemble(this, t);
		
		last_time=t;
		data_control.saveData();
	}
	
	public void updateStatus(long t)
	{
		if(location.owner != null) //do nothing unless the location has an owner
		{
			synchronized(queue_lock)
			{
				if(manufac_queue.size() != 0)
				{
					time_on_current_ship += t-last_time;
					int first = indexOfFirstShipInQueue();
					if(time_on_current_ship >= manufac_queue.get(first).type.time_to_build)
					{
						time_on_current_ship -= manufac_queue.get(first).type.time_to_build;
						produce(t-time_on_current_ship);
						
						//update the queue display... if it is being displayed.
						if(location.owner.getId() == GameInterface.GC.player_id && GameInterface.GC.GI.sat_or_ship_disp == GameInterface.SAT_PANEL_DISP
							&& GameInterface.GC.GI.SatellitePanel.the_sat.equals(location) && GameInterface.GC.GI.SatellitePanel.state == PlanetMoonCommandPanel.SHIP_QUEUE_DISPLAYED
							&& GameInterface.GC.GI.SatellitePanel.the_shipyard == this)
							GameInterface.GC.GI.SatellitePanel.displayQueue();
						
						if(manufac_queue.size() == 0)
							time_on_current_ship = 0;
					}
					last_time=t;
				}
			}
		}
	}
	
	public double percentComplete()
	{
		return ((double)time_on_current_ship)/((double)manufac_queue.get(indexOfFirstShipInQueue()).type.time_to_build);
	}
	
	public int indexOfFirstShipInQueue(){return Collections.min(manufac_queue.keySet());}
	
	public FacilityType getType(){return FacilityType.SHIPYARD;}
	public String imageLoc(){return "images/shipyard.gif";}
	public String getName(){return "Shipyard";}

	@Override
	public void ownerChanged(long t) {
		//do nothing
		//data_control.saveData();
	}
}
