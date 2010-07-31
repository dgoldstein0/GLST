import java.util.HashSet;
import java.util.Set;

public class ShipDataSaverControl extends FlyerDataSaverControl<Ship, ShipDataSaver> {

	public ShipDataSaverControl(Ship s)
	{
		super(s, new Creator<Ship, ShipDataSaver >(){
			public ShipDataSaver create(){return new ShipDataSaver();}
			public ShipDataSaver[] createArray(){return new ShipDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
	
	@Override
	protected ReversionEffects deduceEffectedAfterIndex(int indx)
	{		
		Set<Order> orders = new HashSet<Order>();
		Set<ReversionEffects.RevertObj> objs = new HashSet<ReversionEffects.RevertObj>();
		
		//look at difference between each two consecutive elements of saved_data
		for(int i=indx, j=getNextIndex(i); j != index; i=j, j=getNextIndex(j))
		{
			//add aggressors
			for(Targetter<?> t : saved_data[j].aggr)
			{
				if(!objs.contains(t))
					objs.add(new ReversionEffects.RevertObj(t, saved_data[j].t));
			}
			
			//add the target
			if(!objs.contains(saved_data[j].tgt) && saved_data[j].tgt != null)
				objs.add(new ReversionEffects.RevertObj(saved_data[j].tgt, saved_data[j].t));
				
			if(saved_data[i].md != saved_data[j].md)
			{
				//changed from doing one thing to doing another
				switch(saved_data[j].md)
				{
					case MOVING:
						if(saved_data[i].md != Ship.MODES.EXIT_WARP) //transition from EXIT_WARP to MOVING is automatic
						{
							orders.add(new ShipMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
						}
						break;
					case TARGETTING_TARGET_LOST:
						orders.add(new ShipAttackOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].t /*target_t doesn't matter, because it is only used over network*/, saved_data[j].was_tgt));
						break;
					case ATTACKING:
						orders.add(new ShipAttackOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].t /*target_t doesn't matter, because it is only used over network*/, (Targetable<?>)saved_data[j].dest));
						break;
					case TRAVEL_TO_WARP:
						orders.add(new ShipWarpOrder(the_obj.owner, the_obj, saved_data[j].t, the_obj.warp_destination));
						break;
					case PICKUP_TROOPS:
						orders.add(new ShipPickupTroopsOrder(the_obj.owner, the_obj, saved_data[j].t));
						break;
					case EXIT_WARP:
						//mark fleet of system we arrived in for reversion
						objs.add(new ReversionEffects.RevertObj(saved_data[j].loc.fleets[the_obj.owner.getId()],saved_data[i].t));
						break;
					case IN_WARP:
						//mark fleet of system we left for reversion
						objs.add(new ReversionEffects.RevertObj(saved_data[i].loc.fleets[the_obj.owner.getId()],saved_data[i].t));
						break;
				}
			}
			else
			{
				//could ship have been ordered to do same thing but on different object?
				switch(saved_data[j].md)
				{
					case ATTACKING:
						if(saved_data[i].tgt != saved_data[j].tgt)
						{
							orders.add(new ShipAttackOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].t, saved_data[j].tgt));
						}
						/*note lack of break here.  This is for future compatibility.  It may sometime be possible to
						 fly and attack at the same time, and switch destinations while moving.  This is interface
						 dependent, but is supported here.*/
					case MOVING:
						if(saved_data[i].dest != saved_data[j].dest) //destination has changed
						{
							orders.add(new ShipMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
						}
						break;
					case TRAVEL_TO_WARP:
						if(saved_data[i].w_dest != saved_data[j].w_dest)
						{
							orders.add(new ShipWarpOrder(the_obj.owner, the_obj, saved_data[j].t, the_obj.warp_destination));
						}
						break;
					case PICKUP_TROOPS: //just in case - at this moment, I am probably not going to make this case possible.
						if(saved_data[i].dest != saved_data[j].dest)
						{
							orders.add(new ShipPickupTroopsOrder(the_obj.owner, the_obj, saved_data[j].t));
						}
						break;
				}
			}
		}
		
		return new ReversionEffects(orders, objs);
	}
	
	@Override
	public void doReversionPrep(int indx)
	{
		//this is responsible for reverting the ship into warp or out of warp
		if(saved_data[indx].md == Ship.MODES.IN_WARP)
		{
			the_obj.owner.ships_in_transit.add(the_obj);
		}
		else
		{
			the_obj.owner.ships_in_transit.remove(the_obj);
		}
	}
}
