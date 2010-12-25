public interface Selectable
{
	static final int SATELLITE=0;
	static final int STAR=1;
	static final int SHIP=2;
	static final int FOCUS=3;
	
	//This interface is only used to provide an object type that contains both StellarObjects - planets, stars, asteroids, and moons - and focus #2 of Satellite's orbit.
	public String generateName();
	public int getSelectType(); //returns one of the integers defined above
	public int getTypeNumber(); //should vary based on what image should be used.  Last 2 bits should be based on constants above; rest of bits can be defined by subclasses
	public ImageResource getImage(); //returns image for the object
}