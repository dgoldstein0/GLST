
public strictfp class TaxOffice extends Facility<TaxOffice> {
	
	long add_money;
	double tax_rate;
	long last_resource_time;
	
	public TaxOffice(OwnableSatellite<?> loc, int i, long t)
	{
		super(loc, i, t, GalacticStrategyConstants.initial_taxoffice_endu);
		location.number_taxoffices++;
		tax_rate = GalacticStrategyConstants.DEFAULT_INCOME_RATE;
		data_control = new TaxOfficeDataSaverControl(this);
		
		//set time to the next resource change, and save.  Need to align ourselves to the
		//timing of resource updates, but can't do it via super() call because then our first
		//record doesn't correspond to the time the TaxOffice was built
		last_resource_time = TimeControl.roundUpToNextResourceChange(t);
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
	public FacilityType getType() {
		
		return FacilityType.TAXOFFICE;
	}

	@Override
	public void ownerChanged(long t) {
		last_time = t;
		last_resource_time = TimeControl.roundUpToNextResourceChange(t);
	}

	@Override
	public void updateStatus(long t) {
		tax_rate = calcTaxingrate();
		add_money=0;
		
		if(t-last_resource_time >= GalacticStrategyConstants.TIME_BETWEEN_RESOURCES && location.owner != null) //do nothing unless the location has an owner
		{
			add_money += tax_rate*location.population;
			last_resource_time += GalacticStrategyConstants.TIME_BETWEEN_RESOURCES;
		}
		location.owner.changeMoney(add_money, last_resource_time);
		last_time = t;
	}
	
	public TaxOffice(){}
	public void setTax_rate(double tr){tax_rate=tr;}
	public double getTax_rate(){return tax_rate;}
	public void setAdd_money(long m){add_money=m;}
	public long getAdd_money(){return add_money;}
}
