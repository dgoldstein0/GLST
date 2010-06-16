
public class ShipDataSaver extends FlyerDataSaver {

	int md; //mode
	long next_at_time; //next attacking time
	
	public ShipDataSaver()
	{
		super();
	}
	
	@Override
	public void saveMoreData(Flyer f)
	{
		Ship s = (Ship)f;
		md=s.mode;
		next_at_time=s.nextAttackingtime;
	}
	
	@Override
	public void loadMoreData(Flyer f)
	{
		Ship s = (Ship)f;
		s.mode=md;
		s.nextAttackingtime=next_at_time;
	}
}
