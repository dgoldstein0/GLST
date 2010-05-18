public class GSystemDescriber implements DestDescriber
{
	int id;
	
	public GSystemDescriber(GSystem sys)
	{
		id=sys.id;
	}
	
	public Describable retrieveDestination(Galaxy g)
	{
		return g.systems.get(id);
	}
	
	public GSystemDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
}