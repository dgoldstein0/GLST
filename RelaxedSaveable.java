
public interface RelaxedSaveable<T extends RelaxedSaveable<T>> extends Saveable<T> {
	public abstract long getTime();
}
