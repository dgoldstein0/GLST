import javax.swing.*;

public class BaseStatusUpdater extends FacilityStatusUpdater
{
	JProgressBar health;
	JLabel soldier_label;
	Base the_base;
	
	public BaseStatusUpdater(JProgressBar hp, JLabel s, Base b)
	{
		health = hp;
		soldier_label = s;
		the_base = b;
	}
	
	public void updateFacility()
	{
		health.setValue(the_base.getEndurance()-the_base.getDamage());
		health.setString(Integer.toString(the_base.getEndurance()-the_base.getDamage()));
		soldier_label.setText("Soldiers: " + Integer.toString(the_base.getSoldierInt()));
	}
}
