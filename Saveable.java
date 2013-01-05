
public strictfp interface Saveable<T extends Saveable<T> > {
	public abstract DataSaverControl<T, ? extends DataSaver<T> > getDataControl();
	public abstract void handleDataNotSaved(long time);
	
	public abstract long getTime();
	public abstract void setTime(long t);
}
