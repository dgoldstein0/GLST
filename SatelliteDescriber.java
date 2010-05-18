public class SatelliteDescriber implements DestDescriber
{
	int id;
	DestDescriber boss_describer;
	
	public SatelliteDescriber(Satellite sat)
	{
		id=sat.id;
		boss_describer = sat.orbit.boss.describer();
	}
	
	public Describable retrieveDestination(Galaxy g)
	{
		Describable boss = boss_describer.retrieveDestination(g);
		
		return ((GSystem)boss).orbiting.get(id);
	}
	
	public SatelliteDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public DestDescriber getBoss_describer(){return boss_describer;}
	public void setBoss_describer(DestDescriber b){boss_describer=b;}
}