import java.awt.image.BufferedImage;


public enum ThumbPictResource {
	JUNK(ImageResource.JUNK,50,50), 
	PLANET(ImageResource.PLANET,50,50),
	MOON(ImageResource.MOON,50,50);
	
	BufferedImage Thumbnail;
	private ThumbPictResource(ImageResource resource, int width, int height)
	{
	    Thumbnail = GraphicsUtilities.createThumbnail(resource.image,
                width, height);
	}
}
