import java.awt.image.ImageObserver;
import java.awt.Image;

public class ImageSizer implements ImageObserver
{
	int index;
	boolean got_width;
	boolean got_height;
	
	public ImageSizer(int i)
	{
		index=i;
		got_width=false;
		got_height=false;
	}
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{		
		if((infoflags&ImageObserver.WIDTH) == ImageObserver.WIDTH)
		{
			GalacticStrategyConstants.sTypes[index].width = width;
			got_width = true;
		}
		if((infoflags&ImageObserver.HEIGHT) == ImageObserver.HEIGHT)
		{
			GalacticStrategyConstants.sTypes[index].height = height;
			got_height = true;
		}
		
		return !(got_width && got_height);
	}
}