
import java.util.*;

public class Shipyard extends Facility{

	ArrayList<Ship> manufac_queue;      //manufacture queue
	final int queue_capa=10;						//the capacity of the queue
	int assemble_x;      				//x coord of assemble point
	int assemble_y;						//y coord
	int default_x;						//default coords to create the new ship, then move to assemble point
	int default_y;
	
	public Shipyard() {		
		manufac_queue=new ArrayList<Ship>(queue_capa);
	}	
	
	public void addToQueue(Ship ship)
	{
		manufac_queue.add(ship);
	}	
		
	public void produce()
	{
		Ship newship=new Ship(location.owner,manufac_queue.get(0).name,manufac_queue.get(0).type);         	//produce the 1st one in the queue
		manufac_queue.remove(0);
		newship.setX(default_x);
		newship.setY(default_y);
		newship.assemble(assemble_x,assemble_y);
	}
			
}
