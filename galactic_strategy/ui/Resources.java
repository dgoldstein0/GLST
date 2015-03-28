package galactic_strategy.ui;
import galactic_strategy.game_objects.ShipType;

import java.io.IOException;

public class Resources
{
	public static void preload() throws IOException //GameControl calls this method when it is instantiated
	{
		//load Images
		for(ImageResource r : ImageResource.values())
		{
			// this function may be called multiple times, so this null check speeds things up.
			if (r.image == null)
				r.image = GraphicsUtilities.loadCompatibleImage(r.img_path);
		}
		
		//store metrics from images to ShipTypes
		for(ShipType t : ShipType.values())
		{
			t.imageLoaded();
		}
	}
}
