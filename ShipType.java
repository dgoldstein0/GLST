import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.ImageIcon;

public strictfp enum ShipType
{
			//name			fuel	hull	money	metal	time to build	troops	image					max speed	max ang. vel.	max accel	warp accel	warp speed	warp range	tooltip
	MISSILE	("Missile",		5,		10,		0,		0,		2000,			0,		ImageResource.MISSILE,	.15,		.0015,			.0001,		0.0,		0.0,		0,			"BOOM!  You're dead if you get hit by one of these..."),
	JUNK	("Junk",		20,		100,	100,	100,	10000,			400,	ImageResource.JUNK,		.06,		.0007,			.00003,		.0005,		.0016,		100,		"A basic spacecraft often built from spare parts.  It can shoot, but don't expect it to take a beating.");
	
	
	String name;
	int max_energy;
	int hull;
	int money_cost;
	int metal_cost;
	int soldier_capacity;
	int time_to_build;
	ImageResource img;
	ImageIcon icon;
	
	//used to cache paint of the scaled image of the ship type
	final private HashMap<Color, shipPaintCache> paint_cache;
	
	int width;
	int height;
	int dim;
	
	//physics characteristics
	double max_speed; //px per millisecond
	double max_angular_vel;//radians per millisecond
	double accel_rate; // px/ms per ms
	
	double warp_accel;
	double warp_speed; //px/ms in Galaxy
	int warp_range; //px in Galaxy
	final String tooltip;
	
	private ShipType(String name, int mfuel, int hull, int money, int metal, int time_to_build, int capacity, ImageResource res, double m_speed, double m_ang_vel, double accel, double waccel, double wspeed, int wrange, String tooltip)
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
		
		paint_cache = new HashMap<Color, shipPaintCache>(11);
		for(int i =0; i< GalacticStrategyConstants.DEFAULT_COLORS.length; ++i)
		{
			Color c = GalacticStrategyConstants.DEFAULT_COLORS[i];
			paint_cache.put(c, new shipPaintCache(c));
		}
	}
	
	public TexturePaint getScaledImage(double scale, Color c)
	{
		return paint_cache.get(c).getScaledImage(scale);
	}
	
	public class shipPaintCache
	{
		private double last_scale;
		private TexturePaint scaled_img_as_paint;
		final private ColorTintFilter tint_op;
		
		public shipPaintCache(Color c)
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
}