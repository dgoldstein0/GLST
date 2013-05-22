
public strictfp interface Saveable<T extends Saveable<T> > {
	public abstract DataSaverControl<T> getDataControl();
}
