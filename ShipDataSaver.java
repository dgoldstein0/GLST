
public class ShipDataSaver extends FlyerDataSaver<Ship> {

	int md; //mode
	long next_at_time; //next attacking time
	long arrive_time;
	GSystem w_dest; //its a long shot we'll need it, but just in case
	float sldr;
	double ex_v_x; //calc'd on orderToWarp, consumed last on disengageWarpDrive - issue would be if exit warp then reenter, and stepping back from after exit to before enter
	double ex_v_y; //calc'd on orderToWarp, consumed last on disengageWarpDrive - see above
	double ex_dir;
	double ex_v_len; /*this only needs to be saved/restored if the following scenario is possible:
						at time A a ship is going to warp, but has yet to engageWarpDrive.
						after the ship reaches its destination, it is then ordered to
						warp again. call this time B.
						
						If, after time B, the ship must load data back to time A, the ship will have
						the wrong exit_vec_len because it will have the value it is planning
						to use for its second jump instead of the one it needs to compute arrival
						time for its first. So in order to get a coordination bug here, the shortest possible
						warp time in the galaxy (plus the amount of time to exit warp and issue another
						warp order) must be shorter than the amount we have to step back.
						
						A bit more to the point: exit_vec_len is computed by orderToWarp and
						consumed by engageWarpDrive.  In order to have a problem, we must step back
						from after an orderToWarp to before the last engageWarpDrive.  UNLIKELY.  But possible?
						Well, I suppose, if someone makes a twin system or we allow some sort of wormhole later.*/
	GSystem loc; //like the exit_vec_len issue, in order for location to get corrupted you have to turn back time
					//from after a warp to before it.
	
	public ShipDataSaver()
	{
		super();
	}
	
	protected void doSaveMoreData(Ship s)
	{
		md=s.mode;
		next_at_time=s.nextAttackingtime;
		arrive_time = s.arrival_time;
		sldr = s.soldier;
		w_dest = s.warp_destination;
		ex_v_x = s.exit_vec_x;
		ex_v_y = s.exit_vec_y;
		ex_dir = s.exit_direction;
		ex_v_len = s.exit_vec_len; //necessary? see above note
		loc = s.location;
	}
	
	public void loadData(Ship s)
	{
		super.loadData(s);
		s.mode=md;
		s.nextAttackingtime=next_at_time;
		s.arrival_time=arrive_time;
		s.soldier = sldr;
		s.warp_destination = w_dest;
		s.exit_vec_x = ex_v_x;
		s.exit_vec_y = ex_v_y;
		s.exit_direction = ex_dir;
		s.exit_vec_len = ex_v_len; //necessary? see above note
		s.location = loc;
	}
}
