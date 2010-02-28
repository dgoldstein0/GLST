import javax.swing.JLabel;


public class SystemLabel extends JLabel
{
	GSystem the_sys;
	
	public SystemLabel(GSystem s)
	{
		super(s.name);
		the_sys =s;
	}
}
