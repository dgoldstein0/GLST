import javax.swing.JLabel;

public class SatelliteLabel extends JLabel
{
	Satellite<?> the_sat;
	
	public SatelliteLabel(Satellite<?> s)
	{
		super(s.name);
		the_sat =s;
	}
}