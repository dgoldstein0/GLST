package galactic_strategy.game_objects;

import galactic_strategy.Constants;
import galactic_strategy.ui.ObjBuilder;
import galactic_strategy.ui.QueueUpdater;

import java.util.*;

import javax.swing.SwingUtilities;

public strictfp class Shipyard extends Facility<Shipyard>{

	Hashtable<Integer, Ship> manufac_queue;      //manufacturing queue - the list of ships to build
	int next_queue_id;
	
	//TODO: add getters and setters, make an order to change this, add that order into the deduceEffected in ShipyardDataSaverControl
	double assemble_x;  //x coord of assemble point
	double assemble_y;	//y coord
	
	//right now, these coords are not saved.
	double default_x = 0;	//default coords to create the new ship, then order it to move to assemble point
	double default_y = 0;
	
	long time_on_current_ship;
	
	public Shipyard(OwnableSatellite<?> loc, int i) {
		super(loc, i, Constants.initial_shipyard_endu);
		manufac_queue=new Hashtable<Integer,Ship>(Constants.queue_capa);
		time_on_current_ship = 0;
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
		
		synchronized(location.owner.getMetal_lock()){
			synchronized(location.owner.getMoney_lock()){
				if(location.owner.getMetal() >= met && location.owner.getMoney() >= mon)
					return true;
				else
					return false;
			}
		}
	}
	
	/**has same return value as canBuild, but this is called to actually start building the ship*/
	public boolean addToQueue(ShipType type, long t)
	{
		boolean ret;
		int met = type.metal_cost;
		int mon = type.money_cost;
		
		synchronized(location.owner.getMetal_lock()){
			synchronized(location.owner.getMoney_lock()){
				if(location.owner.getMetal() >= met && location.owner.getMoney() >= mon)
				{
					location.owner.changeMetal(-met);
					location.owner.changeMoney(-mon);
					
					Ship ship = new Ship(type, new ShipId(next_queue_id, this));
					synchronized(manufac_queue)
					{
						manufac_queue.put(next_queue_id++, ship);
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
	
	public void removeFromQueue(Ship ship)
	{
		synchronized(manufac_queue)
		{
			manufac_queue.remove(ship.getId().queue_id);
			
			if(manufac_queue.size() == 0)
			{
				time_on_current_ship=0l;
			}
			else
			{
				//refund money and metal
				synchronized(location.owner.getMetal_lock())
				{
					synchronized(location.owner.getMoney_lock())
					{
						location.owner.changeMoney(ship.type.money_cost);
						location.owner.changeMetal(ship.type.metal_cost);
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
		synchronized(manufac_queue)
		{
			int first = Collections.min(manufac_queue.keySet());
			
			newship=manufac_queue.get(first);//produce the 1st one in the queue
			manufac_queue.remove(first);
		}
		newship.assemble(this);
	}
	
	public void updateStatus(long t)
	{
		if(location.owner != null) //do nothing unless the location has an owner
		{
			synchronized(manufac_queue)
			{
				if(manufac_queue.size() != 0)
				{
					time_on_current_ship += Constants.TIME_GRANULARITY;
					int first = indexOfFirstShipInQueue();
					if(time_on_current_ship >= manufac_queue.get(first).type.time_to_build)
					{
						produce(t);
						
						//update the queue display... if it is being displayed.
						SwingUtilities.invokeLater(new QueueUpdater(this));
						
						time_on_current_ship = 0;
					}

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

	@Override
	public void ownerChanged(long t) {
		//do nothing
	}
}
