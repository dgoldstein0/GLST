import java.awt.*;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.geom.*;

public class SystemPainter extends JPanel
{
	final int GHOST_NONE=0;
	final int GHOST_OBJ=1;
	
	GSystem system;
	List<Selectable> selected;
	
	boolean design_view;
	int focus2_x;
	int focus2_y;
	int x;
	int y;
	
	int ghost_obj;
	int ghost_x;
	int ghost_y;
	int ghost_size;
	
	double scale;
	double center_x;
	double center_y;
	
	boolean game_mode=false;
	boolean draw_select_box;
		double select_x1;
		double select_y1;
		double select_x2;
		double select_y2;
	
	Image return_arrow;
	
	final static int arrow_size = 25;
	
	//cache for mouseover effects
	Selectable mouseover_obj;
	
	public SystemPainter(boolean design)
	{
		setMinimumSize(new Dimension(800,600));
		design_view=design;
		ghost_obj=GHOST_NONE;
		return_arrow=Toolkit.getDefaultToolkit().getImage("images/return_arrow.jpg");
		draw_select_box=false;
		selected = new ArrayList<Selectable>();
		scale = 1.0d;
		
		mouseover_obj = null;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		super.paintComponent(g);
		setBackground(Color.BLACK);
		
		if(design_view) {
			//star zone
			g2.setColor(new Color(255,255,0,100));
			g2.draw(new Ellipse2D.Double(drawX((getWidth()-100)/2),drawY((getHeight()-100)/2),100*scale,100*scale));
			
			//cross marking center of screen
			g2.setColor(Color.RED);
			g2.drawLine(drawXInt(center_x-5),drawYInt(center_y),drawXInt(center_x+5),drawYInt(center_y));
			g2.drawLine(drawXInt(center_x),drawYInt(center_y-5),drawXInt(center_x),drawYInt(center_y+5));
		}
		
		if(system != null)
		{
			//draw stars
			if(system.stars != null)
			{
				for(Star st : system.stars)
				{
					//drawStar
					ImageResource star_img = st.getImage();
					g2.drawImage(star_img.image, drawXInt(st.x-(st.size/2)), drawYInt(st.y-(st.size/2)), (int)(st.size*scale), (int)(st.size*scale), this);
				}
			}
			
			//draw orbiting objects
			if(system.orbiting != null)
			{
				g2.setFont(g2.getFont().deriveFont(Font.BOLD,12.0f));
				FontMetrics m=g2.getFontMetrics(g2.getFont());
				
				for(Satellite<?> orbiting : system.orbiting)
				{
					//draw object
					drawOrbit(orbiting, g2);
					if(orbiting instanceof Planet && ((Planet)orbiting).getOwner() != null)
						g2.setColor(((Planet)orbiting).getOwner().getColor());
					else
						g2.setColor(Color.WHITE);
					
					g2.fill(new Ellipse2D.Double(drawX(orbiting.absoluteCurX()-(orbiting.size/2.0)), drawY(orbiting.absoluteCurY()-(orbiting.size/2.0)), orbiting.size*scale, orbiting.size*scale));
					
					//draw name
					if(orbiting.name.length() == 0) {
						g2.setColor(Color.YELLOW);
						g2.drawString("Unnamed", drawXf(orbiting.absoluteCurX()-orbiting.size/2.0), drawYf(orbiting.absoluteCurY()+orbiting.size/2.0)+(float)(m.getHeight()));
					} else {
						//use the color previously determined.  This should work off of the owner's color or WHITE if there is no owner
						g2.drawString(orbiting.name, drawXf(orbiting.absoluteCurX()-orbiting.size/2.0), drawYf(orbiting.absoluteCurY()+orbiting.size/2.0)+(float)(m.getHeight()));
					}
					
					//draw objects orbiting planets					
					if(orbiting instanceof Planet && ((Planet)orbiting).orbiting != null)
					{
						ArrayList<Satellite<?>> planet_sats = ((Planet)orbiting).orbiting;
						for(Satellite<?> sat : planet_sats)
						{
							drawOrbit(sat, g2);
							if(sat instanceof Moon && ((Moon)sat).getOwner() != null)
								g2.setColor(((Moon)sat).getOwner().getColor());
							else
								g2.setColor(Color.WHITE);
							g2.fill(new Ellipse2D.Double(drawX(sat.absoluteCurX()-sat.size/2), drawY(sat.absoluteCurY()-sat.size/2), sat.size*scale, sat.size*scale));
							
							if(sat.name.length() == 0) {
								g2.setColor(Color.YELLOW);
								g2.drawString("Unnamed", drawXf(sat.absoluteCurX() - sat.size/2.0), drawYf(sat.absoluteCurY()+sat.size/2.0)+(float)(m.getHeight()));
							} else {
								g2.drawString(sat.name, drawXf(sat.absoluteCurX() - sat.size/2.0), drawYf(sat.absoluteCurY()+sat.size/2.0)+(float)(m.getHeight()));
							}
						}
					}
				}
			}
			
			//draw all ships
			for(int i=0; i<system.fleets.length; i++)
			{
				if(system.fleets[i] != null)
				{
					synchronized(system.fleets[i].lock)
					{
						for(Ship.ShipId j : system.fleets[i].ships.keySet())
						{
							Flyer<?,?,?> f = system.fleets[i].ships.get(j);
							drawFlyer(g2,f,system.fleets[i].owner.getColor());
						}
					}
				}
			}
			
			synchronized(system.missiles)
			{
				for(Missile.MissileId i : system.missiles.keySet())
				{
					drawFlyer(g2,(Flyer<Missile, Missile.MissileId, Iterator<Missile.MissileId>>)system.missiles.get(i),null);
				}
			}
			
			g2.setColor(Color.WHITE);
			if(system.name != null)
				g2.drawString(system.name + " System", 10, 20);
			
			if(draw_select_box)
			{
				g2.setColor(Color.GRAY);
				g2.draw(new Rectangle2D.Double(drawX(Math.min(select_x1, select_x2)), drawY(Math.min(select_y1, select_y2)), scale*Math.abs(select_x1-select_x2), scale*Math.abs(select_y1-select_y2)));
			}
		}
		
		for(Selectable obj : selected)
		{
			g2.setColor(Color.WHITE);
			if(obj instanceof Satellite<?>) {
				if(game_mode){
					g2.draw(new Ellipse2D.Double(drawX(((Satellite<?>)obj).absoluteCurX()-((StellarObject)obj).size/2.0)-2.0, drawY(((Satellite<?>)obj).absoluteCurY()-((StellarObject)obj).size/2.0)-2.0, ((StellarObject)obj).size*scale+4.0, ((StellarObject)obj).size*scale+4.0));
				} else {
					g2.draw(new Ellipse2D.Double(drawX(((Satellite<?>)obj).absoluteInitX()-((StellarObject)obj).size/2.0)-2.0, drawY(((Satellite<?>)obj).absoluteInitY()-((StellarObject)obj).size/2.0)-2.0, ((StellarObject)obj).size*scale+4.0, ((StellarObject)obj).size*scale+4.0));
				}
			} else if(obj instanceof Star) {
				//select a star
				g2.draw(new Ellipse2D.Double(drawX(((Star)obj).x-((StellarObject)obj).size/2.0), drawY(((Star)obj).y-((StellarObject)obj).size/2.0), ((StellarObject)obj).size*scale, ((StellarObject)obj).size*scale));
			} else if(obj instanceof Focus) {
				g2.draw(new Ellipse2D.Double(drawX(((Focus)obj).getX()+(((Focus)obj).owner.boss.absoluteCurX()))-2.0, drawY(((Focus)obj).getY()+(((Focus)obj).owner.boss.absoluteCurY()))-2.0, 5.0,5.0));
			} else if(obj instanceof Ship) {
				Ship s = (Ship)obj;
				g2.draw(new Ellipse2D.Double(drawX(s.pos_x - s.type.dim*s.type.img.scale/2.0), drawY(s.pos_y - s.type.dim*s.type.img.scale/2.0), s.type.dim*s.type.img.scale*scale, s.type.dim*s.type.img.scale*scale));
				g2.setColor(Color.ORANGE);
				g2.drawLine(drawXInt(s.dest_x_coord)-3, drawYInt(s.dest_y_coord), drawXInt(s.dest_x_coord)+3, drawYInt(s.dest_y_coord));
				g2.drawLine(drawXInt(s.dest_x_coord), drawYInt(s.dest_y_coord)-3, drawXInt(s.dest_x_coord), drawYInt(s.dest_y_coord)+3);
			}
		}
		
		if(mouseover_obj != null)
		{
			g2.setColor(Color.YELLOW);
			if(mouseover_obj instanceof Satellite<?>) {
				if(game_mode){
					g2.draw(new Ellipse2D.Double(drawX(((Satellite<?>)mouseover_obj).absoluteCurX()-((StellarObject)mouseover_obj).size/2.0)-2.0, drawY(((Satellite<?>)mouseover_obj).absoluteCurY()-((StellarObject)mouseover_obj).size/2.0)-2.0, ((StellarObject)mouseover_obj).size*scale+4.0, ((StellarObject)mouseover_obj).size*scale+4.0));
				} else {
					g2.draw(new Ellipse2D.Double(drawX(((Satellite<?>)mouseover_obj).absoluteInitX()-((StellarObject)mouseover_obj).size/2.0)-2.0, drawY(((Satellite<?>)mouseover_obj).absoluteInitY()-((StellarObject)mouseover_obj).size/2.0)-2.0, ((StellarObject)mouseover_obj).size*scale+4.0, ((StellarObject)mouseover_obj).size*scale+4.0));
				}
			} else if(mouseover_obj instanceof Star) {
				//select a star
				g2.draw(new Ellipse2D.Double(drawX(((Star)mouseover_obj).x-((StellarObject)mouseover_obj).size/2.0), drawY(((Star)mouseover_obj).y-((StellarObject)mouseover_obj).size/2.0), ((StellarObject)mouseover_obj).size*scale, ((StellarObject)mouseover_obj).size*scale));
			} else if(mouseover_obj instanceof Focus) {
				g2.draw(new Ellipse2D.Double(drawX(((Focus)mouseover_obj).getX()+(((Focus)mouseover_obj).owner.boss.absoluteCurX()))-2.0, drawY(((Focus)mouseover_obj).getY()+(((Focus)mouseover_obj).owner.boss.absoluteCurY()))-2.0, 5.0,5.0));
			} else if(mouseover_obj instanceof Ship) {
				Ship s = (Ship)mouseover_obj;
				g2.draw(new Ellipse2D.Double(drawX(s.pos_x - s.type.dim*s.type.img.scale/2.0), drawY(s.pos_y - s.type.dim*s.type.img.scale/2.0), s.type.dim*s.type.img.scale*scale, s.type.dim*s.type.img.scale*scale));
			}
		}
		
		if(ghost_obj==GHOST_OBJ)
		{
			g2.setColor(Color.GRAY);
			g2.draw(new Ellipse2D.Double(drawX(ghost_x-ghost_size/2.0), drawY(ghost_y-ghost_size/2.0), ghost_size*scale, ghost_size*scale));
		}
		
		if(game_mode)
			g2.drawImage(return_arrow, getWidth()-arrow_size, 0, arrow_size, arrow_size, this);
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
		g2.setPaint(s.type.getScaledImage(scale, s.getOwner().getColor()));
		
		g2.translate(drawX(s.getPos_x()-s.type.img.scale*s.type.img.getWidth()/2.0), drawY(s.getPos_y()-s.type.img.scale*s.type.img.getHeight()/2.0));
		g2.rotate(s.direction+Math.PI/2, s.type.img.scale*s.type.img.getWidth()*scale/2.0,s.type.img.scale*s.type.img.getHeight()*scale/2.0);
		g2.fill(new Rectangle2D.Double(0.0, 0.0, s.type.img.scale*s.type.img.getWidth()*scale, s.type.img.scale*scale*s.type.img.getHeight()));
		
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
		ghost_obj=GHOST_NONE;
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
	public void paintSystem(GSystem system, List<Selectable> selected, double centerx, double centery, double sc, boolean back, boolean select_box, double x1, double y1, double x2, double y2, Selectable mouseovered)
	{
		game_mode = back;
		draw_select_box = select_box;
		select_x1 = x1;
		select_y1 = y1;
		select_x2 = x2;
		select_y2 = y2;
		paintSystem(system, selected, centerx, centery, sc, mouseovered);
	}
	
	public void paintGhostObj(GSystem system, List<Selectable> selected, int x, int y, int size, double centerx, double centery, double sc)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_OBJ;
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
		double focus1_x = obj.orbit.boss.absoluteCurX();
		double focus1_y = obj.orbit.boss.absoluteCurY();
		
		double focus2_x = drawX(obj.orbit.focus2.getX()+focus1_x);
		double focus2_y = drawY(obj.orbit.focus2.getY()+focus1_y);
		
		focus1_x=drawX(focus1_x);
		focus1_y=drawY(focus1_y);
		
		double x = drawX(obj.absoluteCurX());
		double y = drawY(obj.absoluteCurY());
		
		if(selected.contains(obj) && !game_mode)
		{
			g2.setColor(Color.RED);
			g2.draw(new Ellipse2D.Double(x,y,2.0,2.0));
			g2.draw(new Ellipse2D.Double(focus1_x-1.0,focus1_y-1.0, 3.0,3.0));
			g2.draw(new Ellipse2D.Double(focus2_x-1.0, focus2_y-1.0, 3.0,3.0));
		}
		
		double a = obj.orbit.a*scale;
		//double c = obj.orbit.c*scale;
		double b = obj.orbit.b*scale;
		
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