import java.util.HashSet;
import java.util.Set;

public strictfp class ShipDataSaverControl extends FlyerDataSaverControl<Ship, ShipDataSaver> {

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
				ReversionEffects.RevertObj o = new ReversionEffects.RevertObj(t, saved_data[j].t-1); //saved_data[j].t-1 is okay because we don't revert those at indx
				objs.add(o);
			}
			
			//add the target
			if(saved_data[j].tgt != null)
			{
				ReversionEffects.RevertObj obj = new ReversionEffects.RevertObj(saved_data[j].tgt, saved_data[j].t-1); //saved_data[j].t-1 is okay because we don't revert those at indx
				objs.add(obj);
			}
			
			//TODO: should second_dest be reverted?
				
			if(saved_data[i].md != saved_data[j].md)
			{
				//changed from doing one thing to doing another
				switch(saved_data[j].md)
				{
					case MOVING:
							orders.add(new ShipMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
						break;
					case TARGET_LOST:
					
						break;
					case USERATTACKING:
						orders.add(new ShipAttackOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].t /*target_t doesn't matter, because it is only used over network*/, (Targetable<?>)saved_data[j].dest));
						break;
					case USERATTACKMOVE:

							orders.add(new ShipAttackMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
						break;
					case TRAVEL_TO_WARP:
						orders.add(new ShipWarpOrder(the_obj.owner, the_obj, saved_data[j].t, the_obj.warp_destination));
						break;
					case PICKUP_TROOPS:
						orders.add(new ShipPickupTroopsOrder(the_obj.owner, the_obj, saved_data[j].t));
						break;
					case EXIT_WARP:
						//mark fleet of system we arrived in for reversion
						objs.add(new ReversionEffects.RevertObj(saved_data[j].loc.fleets[the_obj.owner.getId()],saved_data[j].t-1));
						break;
					case IN_WARP:
						//mark fleet of system we left for reversion
						objs.add(new ReversionEffects.RevertObj(saved_data[i].loc.fleets[the_obj.owner.getId()],saved_data[j].t-1));
						break;
				}
			}
			else
			{
				//could ship have been ordered to do same thing but on different object?
				switch(saved_data[j].md)
				{
					case USERATTACKING:
						if(saved_data[i].tgt != saved_data[j].tgt)
						{
							orders.add(new ShipAttackOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].t, saved_data[j].tgt));
						}
						/*note lack of break here.  This is for future compatibility.  It may sometime be possible to
						 fly and attack at the same time, and switch destinations while moving.  This is interface
						 dependent, but is supported here.  Also, this is necessary since right now, move->attack transition usually means a move and an attack order*/
					case MOVING:
						if(saved_data[i].dest != saved_data[j].dest) //destination has changed
						{
							orders.add(new ShipMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
						}
						break;
					case USERATTACKMOVE:
						if(saved_data[i].dest != saved_data[j].dest && saved_data[i].second_dest != saved_data[j].second_dest) //destination has changed
						{
							orders.add(new ShipAttackMoveOrder(the_obj.owner, the_obj, saved_data[j].t, saved_data[j].dest));
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
		//effectively, this avoids saving the set of ships in warp.  Pretty efficient.
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
