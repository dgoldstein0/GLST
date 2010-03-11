
import java.util.*;

public class Shipyard extends Facility{

	ArrayList<Ship> manufac_queue;      //manufacture queue
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
	
	public void addToQueue(Ship ship, long t)
	{
		manufac_queue.add(ship);
		if(manufac_queue.size() == 1) //the ship must start production immediately.  This marks the start time.
			last_time=t;
	}	
		
	public void produce(long t)
	{
		Ship newship=manufac_queue.get(0);//produce the 1st one in the queue
		manufac_queue.remove(0);
		newship.assemble(this, t);
	}
	
	public void updateStatus(long t)
	{
		if(location.owner instanceof Player) //do nothing unless the location has an owner
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
	
	public double percentComplete()
	{
		return ((double)time_on_current_ship)/((double)manufac_queue.get(0).type.time_to_build);
	}
	
	public int getType(){return Facility.SHIPYARD;}
	public String getImgLoc(){return "images/shipyard.gif";}
	public String getName(){return "Shipyard";}
}
