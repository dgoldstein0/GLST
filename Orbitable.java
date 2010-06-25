import java.util.*;

interface Orbitable<T extends Orbitable<T>> extends Positioning<T>
{
	public ArrayList<Satellite<?>> getOrbiting();
	public void setOrbiting(ArrayList<Satellite<?>> o);
}