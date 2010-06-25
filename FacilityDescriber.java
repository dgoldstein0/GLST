public class FacilityDescriber<T extends Facility<T>> implements Describer<T>
{
	int id;
	Describer<? extends OwnableSatellite<?>> boss;
	
	public FacilityDescriber(T f)
	{
		id = f.id;
		boss=f.location.describer();
	}
	
	@SuppressWarnings("unchecked")
	public T retrieveObject(Galaxy g)
	{
		return (T)boss.retrieveObject(g).facilities.get(id);
	}
	
	public FacilityDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public Describer<? extends OwnableSatellite<?>> getBoss(){return boss;}
	public void setBoss(Describer<? extends OwnableSatellite<?>> b){boss=b;}
}