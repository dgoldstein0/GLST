package galactic_strategy.game_objects;
import galactic_strategy.Constants;
import galactic_strategy.ui.ColorTintFilter;
import galactic_strategy.ui.ImageResource;
import galactic_strategy.ui.ThumbPictResource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;

public strictfp enum ShipType
{
			//name			fuel	hull	money	metal	time to build	troops	image					ThumbPict					max speed	max ang. vel.	max accel	warp accel	warp speed	warp range	tooltip
	MISSILE	("Missile",		5,		10,		0,		0,		2000,			0,		ImageResource.MISSILE,	null,						.15,		.0015,			.0001,		0.0,		0.0,		0,			"BOOM!  You're dead if you get hit by one of these..."),
	JUNK	("Junk",		20,		100,	100,	100,	10000,			400,	ImageResource.JUNK,		ThumbPictResource.JUNK,		.06,		.0007,			.00003,		.0005,		.0016,		100,		"A basic spacecraft often built from spare parts.  It can shoot, but don't expect it to take a beating.");
	
	String name;
	public final int max_energy;
	public final int hull;
	public final int money_cost;
	public final int metal_cost;
	public final int soldier_capacity;
	public final int time_to_build;
	public final ImageResource img;
	ThumbPictResource thumbimg;
	ImageIcon icon;
	
	//used to cache paint of the scaled image of the ship type
	final private HashMap<Color, ShipPaintCache> paint_cache;
	
	private int width;
	private int height;
	private int dim;
	
	//physics characteristics
	public final double max_speed; //px per millisecond
	public final double max_angular_vel;//radians per millisecond
	public final double accel_rate; // px/ms per ms
	
	public final double warp_accel;
	public final double warp_speed; //px/ms in Galaxy
	public final int warp_range; //px in Galaxy
	public final String tooltip;
	
	private ShipType(String name, int mfuel, int hull, int money, int metal, int time_to_build, int capacity, ImageResource res, ThumbPictResource thumbs, double m_speed, double m_ang_vel, double accel, double waccel, double wspeed, int wrange, String tooltip)
	{
		this.name=name;
		max_energy=mfuel;
		this.hull=hull;
		metal_cost = metal;
		money_cost = money;
		this.time_to_build=time_to_build;
		this.soldier_capacity=capacity;
		
		img = res;
		max_speed = m_speed;
		max_angular_vel = m_ang_vel;
		accel_rate = accel;
		warp_accel = waccel;
		warp_speed = wspeed;
		warp_range = wrange;
		this.tooltip = tooltip;
		thumbimg = thumbs;
		
		paint_cache = new HashMap<Color, ShipPaintCache>(11);
		for(int i =0; i< Constants.DEFAULT_COLORS.length; ++i)
		{
			Color c = Constants.DEFAULT_COLORS[i];
			paint_cache.put(c, new ShipPaintCache(c));
		}
	}
	
	public FlyingThing.Capabilities getCapabilities(Flyer<?,?,?> f) {
		return new FlyerCapabilities(f, f.type); 
	}
	
	public TexturePaint getScaledImage(double scale, Color c)
	{
		return paint_cache.get(c).getScaledImage(scale);
	}
	
	public class ShipPaintCache
	{
		private double last_scale;
		private TexturePaint scaled_img_as_paint;
		final private ColorTintFilter tint_op;
		
		public ShipPaintCache(Color c)
		{
			last_scale=0.0; //will never have a value of zero, so this indicates an invalid cache
			scaled_img_as_paint=null;
			tint_op = new ColorTintFilter(c,.25f);
		}
		
		public TexturePaint getScaledImage(double scale)
		{
			if(last_scale != scale)
			{
				BufferedImage tinted;
				BufferedImage scaled_img;
				
				try
				{
					tinted = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(
				            width, height, Transparency.BITMASK);
					scaled_img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(
			            (int)(width*img.scale*scale), (int)(height*img.scale*scale), Transparency.BITMASK);
				}
				catch(HeadlessException he)
				{
					tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					scaled_img = new BufferedImage((int)(img.scale*width*scale), (int)(img.scale*height*scale), BufferedImage.TYPE_INT_ARGB);
				}
				
				tinted = tint_op.filter(img.image, tinted);
				
				Graphics2D temp2 = scaled_img.createGraphics();
				temp2.drawImage(tinted, 0, 0, (int)(img.scale*width*scale), (int)(img.scale*scale*height), null);
				temp2.dispose();
				
				scaled_img_as_paint = new TexturePaint(scaled_img, new Rectangle2D.Double(0, 0, img.scale*width*scale, img.scale*width*scale));
				
				last_scale = scale;
				tinted.flush();
				scaled_img.flush();
			}
			
			return scaled_img_as_paint;
		}
	}

	public void imageLoaded() {
		width = img.getWidth();
		height = img.getHeight();
		dim = Math.max(width, height);
		
		// null check to avoid constructing extra ImageIcons
		if (icon == null)
			icon = new ImageIcon(img.image);
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public int getDim() {
		return dim;
	}

	public void setDim(int dim) {
		this.dim = dim;
	}

	public String getName() {
		return name;
	}
}