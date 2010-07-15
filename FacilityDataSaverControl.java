import java.util.Set;

public abstract class FacilityDataSaverControl<T extends Facility<T>, S extends FacilityDataSaver<T>> extends RelaxedDataSaverControl<T, S> {

	public FacilityDataSaverControl(T fac, Creator<T, S> c)
	{
		super(fac, c);
	}
	
	final protected void addAggressorsAtIndex(int j, Set<ReversionEffects.RevertObj> obj_container)
	{
		for(Targetter<?> t : saved_data[j].aggr)
		{
			if(!obj_container.contains(t))
				obj_container.add(new ReversionEffects.RevertObj(t, saved_data[j].t));
		}
	}
}
