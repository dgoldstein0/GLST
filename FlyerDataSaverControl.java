public strictfp abstract class FlyerDataSaverControl<T extends Flyer<T, ?, ?>, S extends FlyerDataSaver<T>> extends DataSaverControl<T,S> {

	public FlyerDataSaverControl(T f, Creator<T,S> c)
	{
		super(f, c);
	}
}
