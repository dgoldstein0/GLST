import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class FacilityIcon extends JLabel
{
	FacilityType ftype;
	
	public FacilityIcon(String path, FacilityType type)
	{
		super(new ImageIcon(path));
		ftype=type;
	}
}