import javax.swing.JProgressBar;

public class MineStatusUpdater extends FacilityStatusUpdater
{
	JProgressBar health;
	Mine the_mine;
	
	public MineStatusUpdater(JProgressBar hp, Mine m)
	{
		health = hp;
		the_mine = m;
	}
	
	public void updateFacility()
	{
		health.setValue(the_mine.getEndurance()-the_mine.getDamage());
		health.setString(Integer.toString(the_mine.getEndurance()-the_mine.getDamage()));
	}
}