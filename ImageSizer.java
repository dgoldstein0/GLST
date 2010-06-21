import java.awt.image.ImageObserver;
import java.awt.Image;

public class ImageSizer implements ImageObserver
{
	ShipType stype;
	boolean got_width;
	boolean got_height;
	
	public ImageSizer(ShipType t)
	{
		stype = t;
		got_width=false;
		got_height=false;
	}
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{		
		if((infoflags&ImageObserver.WIDTH) == ImageObserver.WIDTH)
		{
			stype.width = width;
			got_width = true;
		}
		if((infoflags&ImageObserver.HEIGHT) == ImageObserver.HEIGHT)
		{
			stype.height = height;
			got_height = true;
		}
		
		if(got_width && got_height)
		{
			stype.dim = Math.max(stype.width, stype.height);
		}
		
		return !(got_width && got_height);
	}
}