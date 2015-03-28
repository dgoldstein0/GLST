package galactic_strategy.ui;
import galactic_strategy.game_objects.Satellite;

import javax.swing.JLabel;

public class SatelliteLabel extends JLabel
{
	private static final long serialVersionUID = -1083111654241830153L;
	Satellite<?> the_sat;
	
	public SatelliteLabel(Satellite<?> s)
	{
		super(s.getName());
		the_sat =s;
	}
}