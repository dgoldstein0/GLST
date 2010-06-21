import java.awt.Image;

public enum ShipType
{
			//name			fuel	hull	money	metal	time to build	troops	default scale	image					max speed	max ang. vel.	max accel	warp accel	warp speed	warp range
	MISSILE	("Missile",		5,		10,		0,		0,		2000,			0,		.20d,			"images/missile.png",	.15,		.0015,			.0001,		0.0,		0.0,		0),
	JUNK	("Junk",		20,		100,	100,	100,	10000,			200,	.30d,			"images/junk.png",		.06,		.0007,			.00003,		.0005,		.0016,		100);
	
	
	String name;
	int max_energy;
	int hull;
	int money_cost;
	int metal_cost;
	int soldier_capacity;
	int time_to_build;
	String img_loc;
	Image img;
	
	double default_scale;
	int width;
	int height;
	int dim;
	
	//physics characteristics
	double max_speed; //px per millisecond
	double max_angular_vel;//radians per milli
	double accel_rate; // px/ms per ms
	
	double warp_accel;
	double warp_speed; //px/ms in Galaxy
	int warp_range; //px in Galaxy
	
	private ShipType(String name, int mfuel, int hull, int money, int metal, int time_to_build, int capacity, double sc, String i, double m_speed, double m_ang_vel, double accel, double waccel, double wspeed, int wrange)
	{
		this.name=name;
		max_energy=mfuel;
		this.hull=hull;
		metal_cost = metal;
		money_cost = money;
		this.time_to_build=time_to_build;
		this.soldier_capacity=capacity;
		default_scale=sc;
		img_loc = i;
		
		max_speed = m_speed;
		max_angular_vel = m_ang_vel;
		accel_rate = accel;
		warp_accel = waccel;
		warp_speed = wspeed;
		warp_range = wrange;
	}
	
	public void setImg(Image i){img=i;}
	public String getImg_loc(){return img_loc;}
}