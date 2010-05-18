public class FacilityDescriber implements DestDescriber
{
	int id;
	DestDescriber boss;
	
	public FacilityDescriber(Facility f)
	{
		id = f.id;
		boss=f.location.describer();
	}
	
	public Describable retrieveDestination(Galaxy g)
	{
		return ((OwnableSatellite)boss.retrieveDestination(g)).facilities.get(id);
	}
	
	public FacilityDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public DestDescriber getBoss(){return boss;}
	public void setBoss(DestDescriber b){boss=b;}
}