
import java.util.*;

public class Shipyard extends Facility{

	ArrayList<Ship> manufac_queue;      //manufacturing queue - the list of ships to build
	Object queue_lock = new Object(); //used to synchronize the queue
	
	double assemble_x;      //x coord of assemble point
	double assemble_y;	//y coord
	double default_x = 0;	//default coords to create the new ship, then order it to move to assemble point
	double default_y = 0;
	
	static GameControl GC;
	
	long time_on_current_ship;
	
	public Shipyard(OwnableSatellite loc, long t) {		
		manufac_queue=new ArrayList<Ship>(GalacticStrategyConstants.queue_capa);
		location = loc;
		damage=0;
		endurance = GalacticStrategyConstants.initial_shipyard_endu;
		time_on_current_ship = 0;
		last_time = t;
	}
	
	public boolean addToQueue(Ship ship, long t)
	{
		int met = ship.type.metal_cost;
		int mon = ship.type.money_cost;
		
		synchronized(location.owner.metal_lock){
			synchronized(location.owner.money_lock){
				if(location.owner.metal >= met && location.owner.money >= mon)
				{
					location.owner.metal -= met;
					location.owner.money -= mon;
					
					synchronized(queue_lock)
					{
						manufac_queue.add(ship);
						if(manufac_queue.size() == 1) //the ship must start production immediately.  This marks the start time.
							last_time=t;
					}
					
					return true;
				}
				else
					return false;
			}
		}
	}
	
	public void removeFromQueue(Ship ship, long t)
	{
		synchronized(queue_lock)
		{
			for(int i=0; i<manufac_queue.size(); i++)
			{
				if(manufac_queue.get(i).getId() == ship.getId())
				{
					manufac_queue.remove(i);
					
					if(i==0)
					{
						time_on_current_ship=0l;
					}
					else
					{
						//refund money and metal
						location.owner.changeMoney(ship.type.money_cost);
						location.owner.changeMetal(ship.type.metal_cost);
					}
					return;
				}
			}
		}
	}
		
	public void produce(long t)
	{
		Ship newship;
		synchronized(queue_lock)
		{
			newship=manufac_queue.get(0);//produce the 1st one in the queue
			manufac_queue.remove(0);
		}
		newship.assemble(this, t);
	}
	
	public void updateStatus(long t)
	{
		if(location.owner instanceof Player) //do nothing unless the location has an owner
		{
			synchronized(queue_lock)
			{
				if(manufac_queue.size() != 0)
				{
					time_on_current_ship += t-last_time;
					if(time_on_current_ship >= manufac_queue.get(0).type.time_to_build)
					{
						time_on_current_ship -= manufac_queue.get(0).type.time_to_build;
						produce(t-time_on_current_ship);
						
						//update the queue display... if it is being displayed.
						if(location.owner.getId() == GC.player_id && GC.GI.sat_or_ship_disp == GameInterface.SAT_PANEL_DISP
							&& GC.GI.SatellitePanel.the_sat.equals(location) && GC.GI.SatellitePanel.state == PlanetMoonCommandPanel.SHIP_QUEUE_DISPLAYED
							&& GC.GI.SatellitePanel.the_shipyard == this)
							GC.GI.SatellitePanel.displayQueue();
						
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
		return ((double)time_on_current_ship)/((double)manufac_queue.get(0).type.time_to_build);
	}
	
	public int getType(){return Facility.SHIPYARD;}
	public String getImgLoc(){return "images/shipyard.gif";}
	public String getName(){return "Shipyard";}
}
