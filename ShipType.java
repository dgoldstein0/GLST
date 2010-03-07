import java.awt.Image;

public class ShipType
{
	String name;
	int max_energy;
	int hull;
	int cost;
	int soldier_capacity;
	int time_to_build;
	String img_loc;
	Image img;
	
	public ShipType(String name, int mfuel, int hull, int cost, int time_to_build, int capacity, String i)
	{
		this.name=name;
		max_energy=mfuel;
		this.hull=hull;
		this.cost=cost;
		this.time_to_build=time_to_build;
		this.soldier_capacity=capacity;
		img_loc = i;
	}
	
	public void setImg(Image i){img=i;}
	public String getImg_loc(){return img_loc;}
}