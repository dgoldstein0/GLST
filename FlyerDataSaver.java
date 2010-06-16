public class FlyerDataSaver
{
	double px; //pos_x and pos_y indicate where in the system the ship is located
	double py;
	double dir; //direction
	double sp; //speed
	long t; //time
	int dmg; //damage

	public FlyerDataSaver()
	{
	}
	
	//the Flyer-only implementations
	public final void saveData(Flyer f)
	{
		dir=f.direction;
		px=f.pos_x;
		py=f.pos_y;
		t=f.time;
		sp=f.speed;
		dmg=f.damage;
	}
	
	public final void loadData(Flyer f)
	{
		f.direction = dir;
		f.pos_x = px;
		f.pos_y = py;
		f.time = t;
		f.speed = sp;
		f.damage=dmg;
	}
	
	//subclasses should override these to provide subclass-specific data saving.
	public void loadMoreData(Flyer f){}
	public void saveMoreData(Flyer f){}
}