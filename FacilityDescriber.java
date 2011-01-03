public strictfp class FacilityDescriber<T extends Facility<T>> implements Describer<T>
{
	int id;
	Describer<? extends OwnableSatellite<?>> boss;
	
	public FacilityDescriber(Facility<T> f)
	{
		id = f.id;
		boss=f.location.describer();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T retrieveObject(Galaxy g, long t)
	{
		OwnableSatelliteDataSaverControl data_ctrl = boss.retrieveObject(g, t).data_control;
		return (T)((OwnableSatelliteDataSaver)data_ctrl.saved_data[data_ctrl.getIndexForTime(t)]).fac.get(id);
	}
	
	public FacilityDescriber(){}
	public int getId(){return id;}
	public void setId(int i){id=i;}
	public Describer<? extends OwnableSatellite<?>> getBoss(){return boss;}
	public void setBoss(Describer<? extends OwnableSatellite<?>> b){boss=b;}
}