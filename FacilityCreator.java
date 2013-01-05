
public abstract class FacilityCreator<T extends Facility<T>> {

	public abstract T create(OwnableSatellite<?> o, int i, long t);
}
