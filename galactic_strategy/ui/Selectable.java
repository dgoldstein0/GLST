package galactic_strategy.ui;

public interface Selectable
{
	static final int SATELLITE=0;
	static final int STAR=1;
	static final int SHIP=2;
	static final int FOCUS=3;
	
	//This interface is only used to provide an object type that contains both StellarObjects - planets, stars, asteroids, and moons - and focus #2 of Satellite's orbit.
	public String generateName();
	public int getSelectType(); //returns one of the integers defined above
	public ImageResource getImage(); //returns image for the object
	
	/**
	 * Method to check if a given point is within or near enough to the Selectable
	 * object that it should be selected.
	 * 
	 * @param x x-coordinate of the point
	 * @param y y-coordinate of the point
	 * @param tolerance how close the point should be to being within the object
	 * @return true if (x, y) is within a box of min_x - tolerance,
	 *   max_x + tolerance, min_y - tolerance, max_y + tolerance, false otherwise.
	 */
	public boolean shouldSelect(double x, double y, double tolerance);
}