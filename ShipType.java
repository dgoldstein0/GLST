public class ShipType
{
	String name;
	int max_energy;
	int hull;
	int cost;
	int soldier_capacity;
	
	public ShipType(String name, int mfuel, int hull, int cost, int capacity)
	{
		this.name=name;
		max_energy=mfuel;
		this.hull=hull;
		this.cost=cost;
		this.soldier_capacity=capacity;
	}
}