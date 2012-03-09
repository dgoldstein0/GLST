
public class TaxOfficeDataSaver extends FacilityDataSaver<TaxOffice> {
	double t_rate;
	long money_added;
	long last_r_time;
	
	TaxOfficeDataSaver()
	{
		super();
	}
	@Override
	protected void doLoadMoreData(TaxOffice f) 
	{
		f.tax_rate = t_rate;
		f.add_money = money_added;
		f.last_resource_time = last_r_time;
	}

	@Override
	protected void doSaveMoreData(TaxOffice f) 
	{
		t_rate = f.tax_rate;
		money_added = f.add_money;
		last_r_time = f.last_resource_time;
	}

}
