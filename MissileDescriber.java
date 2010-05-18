public class MissileDescriber implements DestDescriber
{
	int system_id;
	int missile_id;
	
	public MissileDescriber(Missile m)
	{
		missile_id = m.getId();
		system_id = m.location.getId();
	}
	
	public Describable retrieveDestination(Galaxy g)
	{
		return g.systems.get(system_id).missiles.get(missile_id);
	}
	
	public MissileDescriber(){}
	public int getSystem_id(){return system_id;}
	public void setSystem_id(int i){system_id=i;}
	public int getMissile_id(){return missile_id;}
	public void setMissile_id(int m){missile_id=m;}
}