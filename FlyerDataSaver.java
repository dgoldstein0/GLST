import java.util.HashSet;

public strictfp class FlyerDataSaver<T extends Flyer<T, ?, ?>> extends DataSaver<T>
{
	double px; //pos_x and pos_y indicate where in the system the ship is located
	double py;
	double dir; //direction
	double sp; //speed
	int dmg; //damage
	HashSet<Targetter<?>> aggr;
	FlyerAI ai; // mode seems to determine what goes here
	Destination<?> dest;
	Targetable<?> tgt;
	boolean is_alive;

	public FlyerDataSaver()
	{
		super();
	}
	
	//the Flyer-only implementations
	@SuppressWarnings("unchecked")
	@Override
	final protected void doSaveData(T f)
	{
		doSaveMoreData(f);
		dir=f.direction;
		px=f.pos_x;
		py=f.pos_y;
		sp=f.speed;
		dmg=f.damage;
		aggr=(HashSet<Targetter<?>>) f.aggressors.clone(); //unchecked cast warning
		ai = f.current_flying_AI;
		dest = f.destination;
		tgt = f.target;
		is_alive = f.is_alive;
	}
	
	protected void doSaveMoreData(T f){}
	protected void doLoadMoreData(T f){}
	
	@SuppressWarnings("unchecked")
	@Override
	final protected void doLoadData(T f)
	{
		doLoadMoreData(f);
		f.direction = dir;
		f.pos_x = px;
		f.pos_y = py;
		f.speed = sp;
		f.damage=dmg;
		f.aggressors = (HashSet<Targetter<?>>) aggr.clone(); //unchecked cast warning
		f.current_flying_AI = ai;
		f.destination = dest;
		f.target = tgt;
		f.is_alive = is_alive;
	}
}