public abstract class FlyerDataSaverControl<T extends Flyer<T, ?>, S extends FlyerDataSaver<T>> extends DataSaverControl<T,S> {

	public FlyerDataSaverControl(T f, Creator<T,S> c)
	{
		super(f, c);
	}
	
	public int getIndexForTime(long t)
	{
		int stepback=(int) (Math.floor((the_obj.getTime()-t)/GalacticStrategyConstants.TIME_GRANULARITY) + 1);
		int indx=-1;
		//System.out.println("load data: t is " + Long.toString(t) + " and time is " + Long.toString(time) + ", so step back... " + Integer.toString(stepback));
		if (stepback>50)
		{
			System.out.println("Error loading ship data: the delay is too long"); //BOOKMARK - how should these errors be dealt with
		}
		else
		{
			if (stepback<=index)
				indx=index-stepback;
			else
				indx=index+GalacticStrategyConstants.data_capacity-stepback;			
		}
		return indx;
	}
}
