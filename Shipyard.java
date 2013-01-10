
import java.util.*;

import javax.swing.SwingUtilities;

public strictfp class Shipyard extends Facility<Shipyard>{

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
	}
	
	//for saving/loading data
	public Shipyard(){}
	public Hashtable<Integer, Ship> getManufac_queue(){return manufac_queue;}
	public void setManufac_queue(Hashtable<Integer, Ship> q){manufac_queue = q;}
	public int getNext_queue_id(){return next_queue_id;}
	public void setNext_queue_id(int i){next_queue_id = i;}
	public long getTime_on_current_ship(){return time_on_current_ship;}
	public void setTime_on_current_ship(long t){time_on_current_ship=t;}
	
	/**has same return value as addToQueue, but called by interface to predict whether order will be valid*/
	public boolean canBuild(ShipType type)
	{
		int met = type.metal_cost;
		int mon = type.money_cost;
		
		synchronized(location.owner.metal_lock){
			synchronized(location.owner.money_lock){
				if(location.owner.getMetal() >= met && location.owner.getMoney() >= mon)
					return true;
				else
					return false;
			}
		}
	}
	
	/**has same return value as canBuild, but this is called to actually start building the ship*/
	public boolean addToQueue(Ship ship, long t)
	{
		boolean ret;
		int met = ship.type.metal_cost;
		int mon = ship.type.money_cost;
		
		synchronized(location.owner.metal_lock){
			synchronized(location.owner.money_lock){
				if(location.owner.getMetal() >= met && location.owner.getMoney() >= mon)
				{
					location.owner.changeMetal(-met, t);
					location.owner.changeMoney(-mon, t);
					
					ship.id = new Ship.ShipId(next_queue_id, this);
					synchronized(queue_lock)
					{
						manufac_queue.put(next_queue_id++,ship);
					}
					ret=true;
					
					SwingUtilities.invokeLater(ObjBuilder.shipManufactureFuncs.getCallback(this));
				}
				else
					ret=false;
			}
		}
		return ret;
	}
	
	public void removeFromQueue(Ship ship, long t)
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
				synchronized(location.owner.metal_lock)
				{
					synchronized(location.owner.money_lock)
					{
						location.owner.changeMoney(ship.type.money_cost, t);
						location.owner.changeMetal(ship.type.metal_cost, t);
					}
				}
			}
			
			SwingUtilities.invokeLater(new QueueUpdater(this));
			
			return;
		}
	}
		
	private void produce(long t)
	{
		Ship newship;
		synchronized(queue_lock)
		{
			int first = Collections.min(manufac_queue.keySet());
			
			newship=manufac_queue.get(first);//produce the 1st one in the queue
			manufac_queue.remove(first);
		}
		newship.assemble(this, t);
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
						SwingUtilities.invokeLater(new QueueUpdater(this));
						
						if(manufac_queue.size() == 0)
							time_on_current_ship = 0;
					}

				}
			}
		}
		last_time=t;
	}
	
	public double percentComplete()
	{
		return ((double)time_on_current_ship)/((double)manufac_queue.get(indexOfFirstShipInQueue()).type.time_to_build);
	}
	
	public int indexOfFirstShipInQueue(){return Collections.min(manufac_queue.keySet());}
	
	public FacilityType getType(){return FacilityType.SHIPYARD;}

	@Override
	public void ownerChanged(long t) {
		//do nothing
	}
}
