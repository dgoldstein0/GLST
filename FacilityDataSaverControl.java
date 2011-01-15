import java.util.Set;

public strictfp abstract class FacilityDataSaverControl<T extends Facility<T>, S extends FacilityDataSaver<T>> extends RelaxedDataSaverControl<T, S> {

	public FacilityDataSaverControl(T fac, Creator<T, S> c)
	{
		super(fac, c);
	}
	
	final protected void addAggressorsAtIndex(int j, Set<ReversionEffects.RevertObj> obj_container)
	{
		for(Targetter<?> t : saved_data[j].aggr)
		{
			obj_container.add(new ReversionEffects.RevertObj(t, saved_data[j].t));
		}
	}
}
