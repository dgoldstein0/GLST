
public strictfp class TaxOffice extends Facility<TaxOffice> {
	
	long add_money;
	double tax_rate;
	int last_known_taxoffice_amount;
	
	public TaxOffice(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, t, GalacticStrategyConstants.initial_taxoffice_endu);
		location.number_taxoffices++;
		tax_rate = GalacticStrategyConstants.DEFAULT_INCOME_RATE;
		last_known_taxoffice_amount = -1;
		data_control = new TaxOfficeDataSaverControl(this);
		data_control.saveData();
	}
	
	public void setTax_rate(double tr)
	{
		tax_rate=tr;
	}
	public double getTax_rate()
	{	
		return tax_rate;
	}
	
	public double calcTaxingrate(){
		double modified_tax_rate = GalacticStrategyConstants.DEFAULT_INCOME_RATE;
		for(int j=1;j<location.number_taxoffices;j++){
			modified_tax_rate*=GalacticStrategyConstants.additional_taxoffice_penalty;
		}
		return modified_tax_rate;
	}
	
	@Override
	public void destroyed()
	{
		synchronized(location.facilities)
		{
			is_alive=false;
			location.facilities.remove(id);
			location.number_taxoffices--;
		}
	}
	
	@Override
	public void removeFromGame(long t)
	{
		synchronized(location.facilities)
		{
			location.facilities.remove(id);
			location.number_taxoffices--;
		}
	}
	
	@Override
	public String getName() {return "Tax Office";}

	@Override
	public FacilityType getType() {
		
		return FacilityType.TAXOFFICE;
	}

	@Override
	public String imageLoc() {return "images/TaxOffice.gif";}

	@Override
	public void ownerChanged(long t) {
		last_time = t;
		data_control.saveData();
	}

	@Override
	public void updateStatus(long t) {
		if(last_known_taxoffice_amount!=location.number_taxoffices){
			last_known_taxoffice_amount = location.number_taxoffices;
			tax_rate = calcTaxingrate();
		}
		add_money=0;
		if(t-last_time >= 3000 && location.owner != null) //do nothing unless the location has an owner
		{
			add_money += tax_rate*location.population;
			last_time += 3000;
		}
		location.owner.changeMoney(add_money);
		data_control.saveData();
	}
	
}
