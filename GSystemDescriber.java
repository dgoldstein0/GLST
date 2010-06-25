public class GSystemDescriber implements Describer<GSystem>
{
	int id;
	
	public GSystemDescriber(GSystem sys)
	{
		id=sys.id;
	}
	
	public GSystem retrieveObject(Galaxy g)
	{
		return g.systems.get(id);
	}
	
	public GSystemDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
}