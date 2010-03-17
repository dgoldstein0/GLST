import java.awt.Image;

public class ShipType
{
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
	
	public ShipType(String name, int mfuel, int hull, int money, int metal, int time_to_build, int capacity, double sc, String i)
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
	}
	
	public void setImg(Image i){img=i;}
	public String getImg_loc(){return img_loc;}
}