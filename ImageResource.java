import java.awt.image.BufferedImage;

public enum ImageResource {

	NULL_STAR		("images/null.png",			1.0),
	RED_STAR		("images/red.png",			1.0),
	ORANGE_STAR		("images/orange.png",		1.0),
	YELLOW_STAR		("images/yellow.png",		1.0),
	WHITE_STAR		("images/white.png",		1.0),
	BLUE_STAR		("images/blue.png",			1.0),
	MISSILE			("images/missile.png",		.20),
	JUNK			("images/junk.png",			.30),
	RETURN_ARROW	("images/return_arrow.png",	1.0);
	
	String img_path;
	BufferedImage image;
	double scale;
	
	private ImageResource(String path, double sc)
	{
		img_path = path;
		scale = sc;
	}
	
	public int getWidth()
	{
		return image.getWidth();
	}
	
	public int getHeight()
	{
		return image.getHeight();
	}
}
