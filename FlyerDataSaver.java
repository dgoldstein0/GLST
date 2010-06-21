import java.util.HashSet;

public class FlyerDataSaver<T extends Flyer> extends DataSaver<T>
{
	double px; //pos_x and pos_y indicate where in the system the ship is located
	double py;
	double dir; //direction
	double sp; //speed
	int dmg; //damage
	HashSet<Targetter> aggr;
	FlyerAI ai; // mode seems to determine what goes here
	Destination dest;

	public FlyerDataSaver()
	{
		super();
	}
	
	//the Flyer-only implementations
	@SuppressWarnings("unchecked")
	public void saveData(T f)
	{
		super.saveData(f);
		dir=f.direction;
		px=f.pos_x;
		py=f.pos_y;
		t=f.time;
		sp=f.speed;
		dmg=f.damage;
		aggr=(HashSet<Targetter>) f.aggressors.clone();
		ai = f.current_flying_AI;
		dest = f.destination;
	}
	
	@SuppressWarnings("unchecked")
	public void loadData(T f)
	{
		f.direction = dir;
		f.pos_x = px;
		f.pos_y = py;
		f.time = t;
		f.speed = sp;
		f.damage=dmg;
		f.aggressors = (HashSet<Targetter>) aggr.clone(); //unchecked cast warning
		f.current_flying_AI = ai;
		f.destination= dest;
	}
}