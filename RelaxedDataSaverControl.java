
public abstract class RelaxedDataSaverControl<T extends RelaxedSaveable> extends DataSaverControl<T> {

	public RelaxedDataSaverControl(T s) {
		super(s);
	}

	@Override
	public int getIndexForTime(long t)
	{
		/*figure out where the earliest information we still have is stored.
			either 0, if the whole array has yet to be utilized, or index,
			because this is the next location to save to*/
		
		int begin_indx;
		if(saved_data[index].isDataSaved())
			begin_indx=index;
		else
			begin_indx=0;
		
		return ModifiedBinarySearch(begin_indx,(index == 0) ? GalacticStrategyConstants.data_capacity-1 : index-1, t);
	}

	//earliest: the index for an entry with time earlier than t
	//latest: the index for an entry with time later than t
	//t: the time being searched for
	//return: the index of the entry in saved_data with the greatest time of the entries that is less than t, or -1 if there is no such entry.
	private int ModifiedBinarySearch(int earliest, int latest, long t)
	{
		if(earliest==latest || earliest+1 == latest)
		{
			if(saved_data[earliest].t < t)
				return earliest;
			else
				return -1; //indicates the time being looked for it too old to be found.
		}
		
		int middle_indx = translateNormalToActualIndex((translateToNormalArrayIndex(earliest) + translateToNormalArrayIndex(latest))/2);
		if(saved_data[middle_indx].t == t)
			return middle_indx;
		else if(saved_data[middle_indx].t > t)
			return ModifiedBinarySearch(earliest, middle_indx, t);
		else //if(saved_data[middle_indx].t < t) will be true by trichotomy
			return ModifiedBinarySearch(middle_indx, latest, t);
	}
	
	private int translateToNormalArrayIndex(int i) //translates index+1 to 0 and index to data_capacity-1
	{
		return (i-index+saved_data.length)%saved_data.length;
	}
	
	//the inverse of translateToNormalArrayIndex
	private int translateNormalToActualIndex(int i)
	{
		return (i+index)%saved_data.length;
	}
}
