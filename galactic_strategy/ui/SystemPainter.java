package galactic_strategy.ui;

import galactic_strategy.Constants;
import galactic_strategy.Player;
import galactic_strategy.game_objects.Fleet;
import galactic_strategy.game_objects.Flyer;
import galactic_strategy.game_objects.Focus;
import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.Missile;
import galactic_strategy.game_objects.MissileList;
import galactic_strategy.game_objects.Moon;
import galactic_strategy.game_objects.Orbit;
import galactic_strategy.game_objects.Planet;
import galactic_strategy.game_objects.Satellite;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.game_objects.ShipType;
import galactic_strategy.game_objects.Star;

import java.awt.*;

import javax.swing.*;

import java.util.*;
import java.util.List;
import java.awt.geom.*;

public class SystemPainter extends JPanel
{
	private static final long serialVersionUID = 3923369020454157064L;

	static enum GHOST_OBJS{NO, YES;}
	
	GSystem system;
	List<Selectable> selected;
	
	boolean design_view;
	int focus2_x;
	int focus2_y;
	int x;
	int y;
	
	GHOST_OBJS ghost_obj;
	int ghost_x;
	int ghost_y;
	int ghost_size;
	
	double scale;
	double center_x;
	double center_y;
	
	boolean game_mode;
	boolean draw_arrow;
	boolean draw_select_box;
		double select_x1;
		double select_y1;
		double select_x2;
		double select_y2;
	
	//cache for mouseover effects
	Selectable mouseover_obj;
	
	public SystemPainter(boolean design)
	{
		setMinimumSize(new Dimension(800,600));
		design_view=design;
		ghost_obj=GHOST_OBJS.NO;
		draw_select_box=false;
		selected = new ArrayList<Selectable>();
		scale = 1.0d;
		
		game_mode=false;
		draw_arrow=false;
		
		mouseover_obj = null;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		super.paintComponent(g);
		if (design_view || !Constants.fogofwar || (system.getOwner_id()==GameInterface.GC.getPlayer_id() ||
				system.getOwner_id() == -1)) {
			setBackground(Color.BLACK);
		}
		else {
			setBackground(Color.darkGray);
		}
		if(design_view) {
			//star zone
			g2.setColor(new Color(255,255,0,100));
			g2.draw(new Ellipse2D.Double(drawX((getWidth()-100)/2.0),drawY((getHeight()-100)/2.0),100*scale,100*scale));
			
			//cross marking center of screen
			g2.setColor(Color.RED);
			g2.drawLine(drawXInt(center_x-5), drawYInt(center_y), drawXInt(center_x+5), drawYInt(center_y));
			g2.drawLine(drawXInt(center_x), drawYInt(center_y-5), drawXInt(center_x), drawYInt(center_y+5));
		}
		
		if(system != null)
		{
			//draw stars
			Set<Star> stars = system.getStars();
			if(stars != null)
			{
				for(Star st : stars)
				{
					//drawStar
					ImageResource star_img = st.getImage();
					double size = st.getSize();
					g2.drawImage(star_img.image, drawXInt(st.getX()-(size/2)), drawYInt(st.getY()-(size/2)),
					             (int)(size*scale), (int)(size*scale), this);
				}
			}
			
			//draw orbiting objects
			List<Satellite<?>> orbiting_satellites = system.getOrbiting();
			if(orbiting_satellites != null)
			{
				g2.setFont(g2.getFont().deriveFont(Font.BOLD,12.0f));
				FontMetrics m=g2.getFontMetrics(g2.getFont());
				
				for(Satellite<?> orbiting : orbiting_satellites)
				{
					//draw object
					drawOrbit(orbiting, g2);
					
					if (design_view || !Constants.fogofwar || (system.getOwner_id() == GameInterface.GC.getPlayer_id()
							|| system.getOwner_id()==-1))
					{
						if(orbiting instanceof Planet && ((Planet)orbiting).getOwner() != null)
						{
							g2.setColor(((Planet)orbiting).getOwner().getColor());
							((Planet)orbiting).setlastcolor(((Planet)orbiting).getOwner().getColor());
						}
						else
						{
							g2.setColor(Color.WHITE);
							((Planet)orbiting).setlastcolor(Color.WHITE);
						}
					}
					else
					{
						g2.setColor(((Planet) orbiting).getlastcolor());
					}
					
					double size = orbiting.getSize();
					g2.fill(
						new Ellipse2D.Double(
							drawX(orbiting.absoluteCurX()-(size/2.0)),
							drawY(orbiting.absoluteCurY()-(size/2.0)),
							size*scale,
							size*scale
						)
					);
					
					//draw name
					String name = orbiting.getName();
					if(name.length() == 0) {
						g2.setColor(Color.YELLOW);
						g2.drawString(
							"Unnamed",
							drawXf(orbiting.absoluteCurX() - size/2.0),
							drawYf(orbiting.absoluteCurY() + size/2.0) + (float)(m.getHeight())
						);
					} else {
						//use the color previously determined.  This should work off of the owner's color or WHITE if there is no owner
						g2.drawString(
							name,
							drawXf(orbiting.absoluteCurX() - size/2.0),
							drawYf(orbiting.absoluteCurY() + size/2.0) + (float)(m.getHeight())
						);
					}
					
					//draw objects orbiting planets					
					if(orbiting instanceof Planet)
					{
						List<Satellite<?>> planet_sats = ((Planet)orbiting).getOrbiting();
						if (planet_sats != null) {
							for(Satellite<?> sat : planet_sats)
							{
								drawOrbit(sat, g2);
								if(design_view||!Constants.fogofwar||(system.getOwner_id()==GameInterface.GC.getPlayer_id()||
										system.getOwner_id()==-1))
								{
									if(sat instanceof Moon)
									{
										Moon moon = (Moon) sat;
										Player owner = moon.getOwner();
										if (owner != null) {
											g2.setColor(owner.getColor());
											moon.setlastcolor(owner.getColor());
										}
										else
										{
											g2.setColor(Color.WHITE);
											moon.setlastcolor(Color.WHITE);
										}
									}
								}
								else
								{
									g2.setColor(((Moon)sat).getlastcolor());
								}
								double sat_size = sat.getSize();
								g2.fill(
									new Ellipse2D.Double(
										drawX(sat.absoluteCurX()-sat_size/2),
										drawY(sat.absoluteCurY()-sat_size/2),
										sat_size*scale,
										sat_size*scale
									)
								);
								
								if(sat.getName().length() == 0) {
									g2.setColor(Color.YELLOW);
									g2.drawString("Unnamed", drawXf(sat.absoluteCurX() - sat_size/2.0), drawYf(sat.absoluteCurY()+sat_size/2.0)+(float)(m.getHeight()));
								} else {
									g2.drawString(sat.getName(), drawXf(sat.absoluteCurX() - sat_size/2.0), drawYf(sat.absoluteCurY()+sat_size/2.0)+(float)(m.getHeight()));
								}
							}
						}
					}
				}
			}
			//draw all ships
			if (design_view || !Constants.fogofwar || (system.getOwner_id() == GameInterface.GC.getPlayer_id() || system.getOwner_id()==-1))
			{
				for(Fleet fleet : system.getFleets())
				{
					if(fleet != null && fleet.getOwner() != null)
					{
						Color owner_color = fleet.getOwner().getColor();
						synchronized(fleet)
						{
							for(Ship.ShipId j : fleet.getShips().keySet())
							{
								Flyer<?,?,?> f = fleet.getShips().get(j);
								drawFlyer(g2, f, owner_color);
							}
						}
					}
				}
				
				MissileList missiles = system.getMissiles();
				synchronized(missiles)
				{
					for(Missile.MissileId i : missiles.keySet())
					{
						drawFlyer(g2,(Flyer<Missile, Missile.MissileId, Iterator<Missile.MissileId>>)missiles.get(i),null);
					}
				}
			
				g2.setColor(Color.WHITE);
				if(system.getName() != null)
					g2.drawString(system.getName() + " System", 10, 20);
				
				if(draw_select_box)
				{
					g2.setColor(Color.GRAY);
					g2.draw(new Rectangle2D.Double(
							drawX(Math.min(select_x1, select_x2)),
							drawY(Math.min(select_y1, select_y2)),
							scale*Math.abs(select_x1-select_x2),
							scale*Math.abs(select_y1-select_y2)
					));
				}
			}
		}
		if (design_view || !Constants.fogofwar || (system.getOwner_id() == GameInterface.GC.getPlayer_id() ||
				system.getOwner_id() == -1))
		{
			for (Selectable obj : selected)
			{
				g2.setColor(Color.WHITE);
				if(obj instanceof Satellite<?>) {
					Satellite<?> sat = ((Satellite<?>)obj);
					if(game_mode){
						g2.draw(new Ellipse2D.Double(
								drawX(sat.absoluteCurX() - sat.getSize()/2.0)-2.0,
								drawY(sat.absoluteCurY() - sat.getSize()/2.0)-2.0,
								sat.getSize()*scale+4.0,
								sat.getSize()*scale+4.0
						));
					} else {
						g2.draw(new Ellipse2D.Double(
								drawX(sat.absoluteInitX() - sat.getSize()/2.0)-2.0,
								drawY(sat.absoluteInitY() - sat.getSize()/2.0)-2.0,
								sat.getSize()*scale+4.0,
								sat.getSize()*scale+4.0
						));
					}
				} else if(obj instanceof Star) {
					//select a star
					Star star = (Star) obj;
					g2.draw(new Ellipse2D.Double(
							drawX(star.getX() - star.getSize()/2.0),
							drawY(star.getY() - star.getSize()/2.0),
							star.getSize()*scale,
							star.getSize()*scale
					));
				} else if(obj instanceof Focus) {
					Focus f = (Focus) obj;
					g2.draw(new Ellipse2D.Double(
							drawX(f.getX()+(f.getOwner().getBoss().absoluteCurX()))-2.0,
							drawY(f.getY()+(f.getOwner().getBoss().absoluteCurY()))-2.0,
							5.0,
							5.0
					));
				} else if(obj instanceof Ship) {
					Ship s = (Ship)obj;
					double ship_dim = s.getType().getDim()*s.getType().img.scale;
					g2.draw(new Ellipse2D.Double(
							drawX(s.getPos_x() - ship_dim/2.0),
							drawY(s.getPos_y() - ship_dim/2.0),
							ship_dim*scale,
							ship_dim*scale
					));
					g2.setColor(Color.ORANGE);
					g2.drawLine(
							drawXInt(s.getDest_x_coord())-3,
							drawYInt(s.getDest_y_coord()),
							drawXInt(s.getDest_x_coord())+3,
							drawYInt(s.getDest_y_coord())
					);
					g2.drawLine(
							drawXInt(s.getDest_x_coord()),
							drawYInt(s.getDest_y_coord())-3,
							drawXInt(s.getDest_x_coord()),
							drawYInt(s.getDest_y_coord())+3
					);
				}
			}
			if(mouseover_obj != null)
			{
				g2.setColor(Color.YELLOW);
				if(mouseover_obj instanceof Satellite<?>) {
					Satellite<?> sat = (Satellite<?>) mouseover_obj;
					double sat_size = sat.getSize();
					if(game_mode){
						g2.draw(new Ellipse2D.Double(
								drawX(sat.absoluteCurX()-sat_size/2.0)-2.0, 
								drawY(sat.absoluteCurY()-sat_size/2.0)-2.0,
								sat_size*scale+4.0,
								sat_size*scale+4.0
						));
					} else {
						g2.draw(new Ellipse2D.Double(
								drawX(sat.absoluteInitX()-sat_size/2.0)-2.0,
								drawY(sat.absoluteInitY()-sat_size/2.0)-2.0,
								sat_size*scale+4.0,
								sat_size*scale+4.0
						));
					}
				} else if(mouseover_obj instanceof Star) {
					Star star = (Star) mouseover_obj;
					//select a star
					double size = star.getSize();
					g2.draw(new Ellipse2D.Double(
							drawX(star.getX()-size/2.0),
							drawY(star.getY()-size/2.0),
							size*scale,
							size*scale
					));
				} else if(mouseover_obj instanceof Focus) {
					Focus f = (Focus) mouseover_obj;
					g2.draw(new Ellipse2D.Double(
							drawX(f.getX()+(f.getOwner().getBoss().absoluteCurX()))-2.0,
							drawY(f.getY()+(f.getOwner().getBoss().absoluteCurY()))-2.0,
							5.0,
							5.0
					));
				} else if(mouseover_obj instanceof Ship) {
					Ship s = (Ship)mouseover_obj;
					double ship_draw_size = s.getType().getDim()*s.getType().img.scale; 
					g2.draw(new Ellipse2D.Double(
							drawX(s.getPos_x() - ship_draw_size/2.0),
							drawY(s.getPos_y() - ship_draw_size/2.0),
							ship_draw_size*scale,
							ship_draw_size*scale
					));
				}
			}
		
			if(ghost_obj==GHOST_OBJS.YES)
			{
				g2.setColor(Color.GRAY);
				g2.draw(new Ellipse2D.Double(drawX(ghost_x-ghost_size/2.0), drawY(ghost_y-ghost_size/2.0), ghost_size*scale, ghost_size*scale));
			}
		}
		
		if(draw_arrow)
		{
			ImageResource ret_arrow = ImageResource.RETURN_ARROW;
			g2.drawImage(ret_arrow.image, getWidth()-ret_arrow.getWidth(), 0, ret_arrow.getWidth(), ret_arrow.getHeight(), this);
		}
	}
	
	private void drawFlyer(Graphics2D g2, Flyer<?,?,?> s, Color c)
	{
		// Get the current transform
		AffineTransform saveAT = g2.getTransform();
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		//draw ship s
		//old direct image drawing
		//g2.rotate(s.direction+Math.PI/2, drawX(s.getPos_x()),drawY(s.getPos_y()));
		//g2.drawImage(s.type.img, drawXInt(s.getPos_x()-s.type.default_scale*s.type.img.getWidth(this)/2.0), drawYInt(s.getPos_y()-s.type.default_scale*s.type.img.getHeight(this)/2.0), (int)(s.type.default_scale*s.type.img.getWidth(this)*scale), (int)(s.type.default_scale*scale*s.type.img.getHeight(this)), this);
		
		
		//idea of how to implement antialiasing from http://weblogs.java.net/blog/2007/03/10/java-2d-trickery-antialiased-image-transforms
		//this also allows me to use double coordinates instead of int's.  :)
		ShipType type = s.getType();
		g2.setPaint(type.getScaledImage(scale, s.getOwner().getColor()));
		
		ImageResource img = type.img;
		g2.translate(
				drawX(s.getPos_x()-img.scale*img.getWidth()/2.0),
				drawY(s.getPos_y()-img.scale*img.getHeight()/2.0)
			);
		g2.rotate(
				s.getDirection() + Math.PI/2,
				img.scale*img.getWidth()*scale/2.0,
				img.scale*img.getHeight()*scale/2.0
			);
		g2.fill(new Rectangle2D.Double(0.0, 0.0, img.scale*img.getWidth()*scale, img.scale*scale*img.getHeight()));
		
		/*if(c != null)
		{
			g2.setColor(c);
			//different transform means different draw command needed
			//g2.draw(new Rectangle2D.Double(drawX(s.getPos_x())-3.0*scale, drawY(s.getPos_y())-3.0*scale,6.0*scale,6.0*scale));
			g2.draw(new Rectangle2D.Double(s.type.img.scale*s.type.img.getWidth()*scale/2.0-3.0*scale, s.type.img.scale*s.type.img.getHeight()*scale/2.0-3.0*scale,6.0*scale,6.0*scale));
		}
		else*/
		{
			/*this fixed a bug where if there are multiple missiles drawn on the screen,
			they are drawn as essentially viewports to one underlying paint, i.e. only one
			missile displays correctly*/
			
			g2.setColor(Color.BLACK);
			g2.drawRect(0,0,0,0); //draw nothing... seems to be necessary
		}
		
		// Restore original transform
		g2.setTransform(saveAT);
	}
	
	public void paintSystem(GSystem system, List<Selectable> selected, double centerx, double centery, double sc, Selectable mouseovered)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_OBJS.NO;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		
		mouseover_obj = mouseovered;
		
		repaint();
	}
	
	public void paintSystem(GSystem system, List<Selectable> selected, boolean view, double centerx, double centery, double sc, Selectable mouseovered)
	{
		design_view=view;
		paintSystem(system, selected, centerx, centery, sc, mouseovered);
	}
	
	//for multi-select box
	public void paintSystem(GSystem system, List<Selectable> selected, double centerx, double centery, double sc, boolean game_mode, boolean select_box, double x1, double y1, double x2, double y2, Selectable mouseovered)
	{
		this.game_mode = game_mode;
		draw_select_box = select_box;
		select_x1 = x1;
		select_y1 = y1;
		select_x2 = x2;
		select_y2 = y2;
		paintSystem(system, selected, centerx, centery, sc, mouseovered);
	}
	
	public void setArrow(boolean arrow)
	{
		draw_arrow=arrow;
	}
	
	public void paintGhostObj(GSystem system, List<Selectable> selected, int x, int y, int size, double centerx, double centery, double sc)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_OBJS.YES;
		ghost_x=x;
		ghost_y=y;
		ghost_size=size;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		repaint();
	}
	
	//drawX and drawY convert from data coordinates to place on the screen
	
	public double drawX(double the_x) //the_x is pixels from upper left corner
	{
		return (the_x-center_x)*scale+((double)getWidth())/2.0d;
	}
	
	public double drawY(double the_y)
	{
		return (the_y-center_y)*scale+((double)getHeight())/2.0d;
	}
	
	public int drawXInt(double the_x)
	{
		return (int)drawX(the_x);
	}
	
	public int drawYInt(double the_y)
	{
		return (int)drawY(the_y);
	}
	
	public float drawXf(double the_x)
	{
		return (float)drawX(the_x);
	}
	
	public float drawYf(double the_y)
	{
		return (float)drawY(the_y);
	}
	
	private void drawOrbit(Satellite<?> obj, Graphics2D g2)
	{		
		Orbit o = obj.getOrbit();
		double focus1_x = o.getBoss().absoluteCurX();
		double focus1_y = o.getBoss().absoluteCurY();
		
		double focus2_x, focus2_y, a,b,x,y;
		synchronized(o)
		{
			Focus focus2 = o.getFocus2();
			focus2_x = drawX(focus2.getX()+focus1_x);
			focus2_y = drawY(focus2.getY()+focus1_y);
			
			a = o.getA()*scale;
			b = o.getB()*scale;
			
			x = drawX(obj.absoluteCurX());
			y = drawY(obj.absoluteCurY());
		}
		
		focus1_x=drawX(focus1_x);
		focus1_y=drawY(focus1_y);
		
		if(selected.contains(obj) && !game_mode)
		{
			g2.setColor(Color.RED);
			g2.draw(new Ellipse2D.Double(x,y,2.0,2.0));
			g2.draw(new Ellipse2D.Double(focus1_x-1.0,focus1_y-1.0, 3.0,3.0));
			g2.draw(new Ellipse2D.Double(focus2_x-1.0, focus2_y-1.0, 3.0,3.0));
		}
		
		
		
		double theta;
		if(focus2_x != focus1_x)
			theta = Math.atan(((double)(focus2_y-focus1_y))/(focus2_x-focus1_x));
		else if(focus2_y>focus1_y)
			theta=Math.PI/2;
		else if(focus2_y < focus1_y)
			theta=-Math.PI/2;
		else
			theta=0;
		
		g2.rotate(theta);
		
		double center_x=(focus1_x+focus2_x)/2;
		double center_y=(focus1_y+focus2_y)/2;
		
		double start_x=center_x*Math.cos(theta)+center_y*Math.sin(theta)-a;
		double start_y=-center_x*Math.sin(theta)+center_y*Math.cos(theta)-b;
		
		if(selected.contains(obj))
			g2.setColor(Color.YELLOW);
		else
			g2.setColor(Color.GRAY);
		g2.draw(new Ellipse2D.Double(start_x, start_y, 2.0*a, 2.0*b));
		
		g2.rotate(-theta);
	}
}