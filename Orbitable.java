import java.util.*;

interface Orbitable extends Positioning
{
	ArrayList<Satellite> orbiting = null;
	
	public ArrayList<Satellite> getOrbiting();
	public void setOrbiting(ArrayList<Satellite> o);
}