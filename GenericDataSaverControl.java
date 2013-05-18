
public class GenericDataSaverControl<T extends Saveable<T>> extends DataSaverControl<T, GenericDataSaver<T>> {

	public GenericDataSaverControl(T o) {
		super(o, new Creator<T, GenericDataSaver<T> >(){
			public GenericDataSaver<T> create(T t){return new GenericDataSaver<T>(t);}
			@SuppressWarnings("unchecked")
			public GenericDataSaver[] createArray(){return new GenericDataSaver[GalacticStrategyConstants.data_capacity];}
		});
	}
	
}
