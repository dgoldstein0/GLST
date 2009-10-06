public class Fleet
{
	String name;
	Ship[] ships;
	Planet location;
	Planet destination;
	
	public Fleet(String nm)
	{
		name=nm;
	}
	
	//methods required for load/save
	public Fleet(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public Ship[] getShips(){return ships;}
	public void setShips(Ship[] sh){ships=sh;}
	public Planet getLocation(){return location;}
	public void setLocation(Planet p){location=p;}
	public Planet getDestination(){return destination;}
	public void setDestination(Planet p){destination=p;}
	
	public void add(Ship s)
	{
		
		if(ships instanceof Ship[])
		{
			Ship[] oldships=new Ship[ships.length];
			for(int i=0; i<oldships.length; i++)
			{
				oldships[i]=ships[i];
			}
			ships = new Ship[oldships.length+1];
			for(int i=0; i<ships.length-1; i++)
			{
				ships[i]=oldships[i];
			}
			ships[ships.length-1]=s;
		}
		else
		{
			ships=new Ship[1];
			ships[0]=s;
		}
	}
}