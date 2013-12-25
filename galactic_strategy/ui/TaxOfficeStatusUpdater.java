package galactic_strategy.ui;
import galactic_strategy.game_objects.TaxOffice;

import javax.swing.JProgressBar;


public strictfp class TaxOfficeStatusUpdater extends FacilityStatusUpdater 
{
	
	JProgressBar health;
	TaxOffice the_office;

	public TaxOfficeStatusUpdater(JProgressBar hp, TaxOffice t)
	{
		health = hp;
		the_office = t;
	}
	@Override
	public void updateFacility() {
		// TODO Auto-generated method stub
		health.setValue(the_office.getEndurance()-the_office.getDamage());
		health.setString(Integer.toString(the_office.getEndurance()-the_office.getDamage()));
	}

}
