import java.io.IOException;
import javax.swing.ImageIcon;

public class Resources
{
	public static void Preload() throws IOException //GameControl calls this method when it is instantiated
	{
		//load Images
		for(ImageResource r : ImageResource.values())
		{
			r.image = GraphicsUtilities.loadCompatibleImage(r.img_path);
		}
		
		//store metrics from images to ShipTypes
		for(ShipType t : ShipType.values())
		{
			t.width = t.img.getWidth();
			t.height = t.img.getHeight();
			t.dim = Math.max(t.width, t.height);
			t.icon = new ImageIcon(t.img.image);
		}
	}
}
