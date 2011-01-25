
public class TaxOfficeDataSaver extends FacilityDataSaver<TaxOffice> {
	double t_rate;
	long money_added;
	TaxOfficeDataSaver()
	{
		super();
	}
	@Override
	protected void doLoadMoreData(TaxOffice f) 
	{
		// TODO Auto-generated method stub
		f.tax_rate = t_rate;
		f.add_money = money_added;
	}

	@Override
	protected void doSaveMoreData(TaxOffice f) 
	{
		// TODO Auto-generated method stub
		t_rate = f.tax_rate;
		money_added = f.add_money;
	}

}
