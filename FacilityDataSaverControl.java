public strictfp abstract class FacilityDataSaverControl<T extends Facility<T>, S extends FacilityDataSaver<T>> extends RelaxedDataSaverControl<T, S> {

	public FacilityDataSaverControl(T fac, Creator<T, S> c)
	{
		super(fac, c);
	}
}
