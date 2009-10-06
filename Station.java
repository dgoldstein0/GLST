public class Station extends Satellite
{
	String name;
	
	public Station(String name)
	{
		this.name=name;
	}
	
	public Station(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
}