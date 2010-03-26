import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.geom.AffineTransform;

public class SystemPainter extends JPanel
{
	final int GHOST_NONE=0;
	final int GHOST_OBJ=1;
	
	GSystem system;
	Selectable selected;
	
	boolean design_view;
	int focus2_x;
	int focus2_y;
	int x;
	int y;
	
	int ghost_obj;
	int ghost_x;
	int ghost_y;
	int ghost_size;
	
	double scale=1.0d; //always greater than or equal to 1
	double center_x;
	double center_y;
	
	boolean game_mode=false;
	Image return_arrow;
	
	static int arrow_size = 25;
	
	public SystemPainter(boolean design)
	{
		setMinimumSize(new Dimension(800,600));
		design_view=design;
		ghost_obj=GHOST_NONE;
		return_arrow=Toolkit.getDefaultToolkit().getImage("images/return_arrow.jpg");
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		setBackground(Color.BLACK);
		
		if(design_view) {
			//star zone
			g.setColor(new Color(255,255,0,100));
			g.drawOval(drawX((getWidth()-100)/2),drawY((getHeight()-100)/2),(int)(100*scale),(int)(100*scale));
			
			//cross marking center of screen
			g.setColor(Color.RED);
			g.drawLine(drawX(center_x-5),drawY(center_y),drawX(center_x+5),drawY(center_y));
			g.drawLine(drawX(center_x),drawY(center_y-5),drawX(center_x),drawY(center_y+5));
		}
		
		if(system instanceof GSystem)
		{
			//draw stars
			if(system.stars instanceof HashSet)
			{
				for(Star st : system.stars)
				{
					//drawStar
					Image star_img = Toolkit.getDefaultToolkit().getImage(GalacticStrategyConstants.color_choice[st.color]);
					g.drawImage(star_img, drawX(st.x-(st.size/2)), drawY(st.y-(st.size/2)), (int)(st.size*scale), (int)(st.size*scale), this);
				}
			}
			
			//draw orbiting objects
			if(system.orbiting_objects instanceof ArrayList)
			{
				g.setFont(g.getFont().deriveFont(Font.BOLD,12.0f));
				FontMetrics m=g.getFontMetrics(g.getFont());
				
				for(Satellite orbiting : system.orbiting_objects)
				{
					//draw object
					drawOrbit(orbiting, g);
					if(orbiting instanceof Planet && ((Planet)orbiting).getOwner() instanceof Player)
						g.setColor(((Planet)orbiting).getOwner().getColor());
					else
						g.setColor(Color.WHITE);
					g.fillOval(drawX(orbiting.absoluteCurX()-(orbiting.size/2.0)), drawY(orbiting.absoluteCurY()-(orbiting.size/2.0)), (int)(orbiting.size*scale), (int)(orbiting.size*scale));
					
					//draw name
					if(orbiting.name.length() == 0) {
						g.setColor(Color.YELLOW);
						g.drawString("Unnamed", drawX(orbiting.absoluteCurX()-orbiting.size/2.0), drawY(orbiting.absoluteCurY()+orbiting.size/2.0)+m.getHeight());
					} else {
						//use the color previously determined.  This should work off of the owner's color or WHITE if there is no owner
						g.drawString(orbiting.name, drawX(orbiting.absoluteCurX()-orbiting.size/2.0), drawY(orbiting.absoluteCurY()+orbiting.size/2.0)+m.getHeight());
					}
					
					//draw objects orbiting planets					
					if(orbiting instanceof Planet && ((Planet)orbiting).satellites instanceof ArrayList)
					{
						ArrayList<Satellite> planet_sats = ((Planet)orbiting).satellites;
						for(Satellite sat : planet_sats)
						{
							drawOrbit(sat, g);
							if(sat instanceof Moon && ((Moon)sat).getOwner() instanceof Player)
								g.setColor(((Moon)sat).getOwner().getColor());
							else
								g.setColor(Color.WHITE);
							g.fillOval(drawX(sat.absoluteCurX()-sat.size/2), drawY(sat.absoluteCurY()-sat.size/2), (int)(sat.size*scale), (int)(sat.size*scale));
							
							if(sat.name.length() == 0) {
								g.setColor(Color.YELLOW);
								g.drawString("Unnamed", drawX(sat.absoluteCurX() - sat.size/2.0), drawY(sat.absoluteCurY()+sat.size/2.0)+m.getHeight());
							} else {
								g.drawString(sat.name, drawX(sat.absoluteCurX() - sat.size/2.0), drawY(sat.absoluteCurY()+sat.size/2.0)+m.getHeight());
							}
						}
					}
				}
			}
			
			Graphics2D g2 = (Graphics2D) g;
			
			//draw all ships
			for(int i=0; i<system.fleets.length; i++)
			{
				if(system.fleets[i] instanceof Fleet)
				{
					for(Integer j : system.fleets[i].ships.keySet())
					{
						Ship s = system.fleets[i].ships.get(j);
						// Get the current transform
						AffineTransform saveAT = g2.getTransform();
						
						//draw ship s
						g2.rotate(s.direction+Math.PI/2, drawXdoub(s.getPos_x()),drawYdoub(s.getPos_y()));
						
						g2.drawImage(s.type.img, drawX(s.getPos_x()-s.type.default_scale*s.type.img.getWidth(this)/2), drawY(s.getPos_y()-s.type.default_scale*s.type.img.getHeight(this)/2), (int)(s.type.default_scale*s.type.img.getWidth(this)*scale), (int)(s.type.default_scale*scale*s.type.img.getHeight(this)), this);
						g2.setColor(system.fleets[i].owner.getColor());
						g2.drawRect((int)(drawXdoub(s.getPos_x())-3.0*scale), (int)(drawYdoub(s.getPos_y())-3.0*scale),(int)(6.0*scale),(int)(6.0*scale));
						
						// Restore original transform
						g2.setTransform(saveAT);
					}
				}
			}
			
			g.setColor(Color.WHITE);
			if(system.name instanceof String)
				g.drawString(system.name + " System", 10, 20);
		}
		
		if(selected instanceof Satellite) {
			g.setColor(Color.YELLOW);
			if(game_mode){
				g.drawOval(drawX(((Satellite)selected).absoluteCurX()-((StellarObject)selected).size/2)-2, drawY(((Satellite)selected).absoluteCurY()-((StellarObject)selected).size/2)-2, (int)(((StellarObject)selected).size*scale)+4, (int)(((StellarObject)selected).size*scale)+4);
			} else {
				g.drawOval(drawX(((Satellite)selected).absoluteInitX()-((StellarObject)selected).size/2)-2, drawY(((Satellite)selected).absoluteInitY()-((StellarObject)selected).size/2)-2, (int)(((StellarObject)selected).size*scale)+4, (int)(((StellarObject)selected).size*scale)+4);
			}
		} else if(selected instanceof Star) {
			//select a star
			g.setColor(Color.YELLOW);
			g.drawOval(drawX(((Star)selected).x-((StellarObject)selected).size/2), drawY(((Star)selected).y-((StellarObject)selected).size/2), (int)(((StellarObject)selected).size*scale), (int)(((StellarObject)selected).size*scale));
		} else if(selected instanceof Focus) {
			g.setColor(Color.YELLOW);
			g.drawOval(drawX(((Focus)selected).getX()+(((Focus)selected).owner.boss.absoluteCurX()))-2, drawY(((Focus)selected).getY()+(((Focus)selected).owner.boss.absoluteCurY()))-2, 5,5);
		} else if(selected instanceof Ship) {
			g.setColor(Color.YELLOW);
			Ship s = (Ship)selected;
			g.drawOval(drawX(s.pos_x - s.type.dim*s.type.default_scale/2.0), drawY(s.pos_y - s.type.dim*s.type.default_scale/2.0), (int)(s.type.dim*s.type.default_scale*scale), (int)(s.type.dim*s.type.default_scale*scale));
			g.setColor(Color.RED);
			g.drawLine(drawX(s.dest_x_coord)-3, drawY(s.dest_y_coord), drawX(s.dest_x_coord)+3, drawY(s.dest_y_coord));
			g.drawLine(drawX(s.dest_x_coord), drawY(s.dest_y_coord)-3, drawX(s.dest_x_coord), drawY(s.dest_y_coord)+3);
		}
		
		if(ghost_obj==GHOST_OBJ)
		{
			g.setColor(Color.GRAY);
			g.drawOval(drawX(ghost_x-ghost_size/2), drawY(ghost_y-ghost_size/2), (int)(ghost_size*scale), (int)(ghost_size*scale));
		}
		
		if(game_mode)
			g.drawImage(return_arrow, getWidth()-arrow_size, 0, arrow_size, arrow_size, this);
	}
	
	public void paintSystem(GSystem system, Selectable selected, boolean view, double centerx, double centery, double sc)
	{
		this.system=system;
		this.selected=selected;
		design_view=view;
		ghost_obj=GHOST_NONE;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		repaint();
	}
	
	public void paintSystem(GSystem system, Selectable selected, double centerx, double centery, double sc)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_NONE;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		repaint();
	}
	
	public void paintSystem(GSystem system, Selectable selected, double centerx, double centery, double sc, boolean back)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_NONE;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		game_mode = back;
		repaint();
	}
	
	public void paintGhostObj(GSystem system, Selectable selected, int x, int y, int size, double centerx, double centery, double sc)
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
	
	public int drawX(double the_x) //the_x is pixels from upper left corner
	{
		return (int)drawXdoub(the_x);
	}
	
	public double drawXdoub(double the_x) //the_x is pixels from upper left corner
	{
		return (the_x-center_x)*scale+((double)getWidth())/2.0d;
	}

	public int drawY(double the_y)
	{
		return (int)drawYdoub(the_y);
	}
	
	public double drawYdoub(double the_y)
	{
		return (the_y-center_y)*scale+((double)getHeight())/2.0d;
	}
	
	private void drawOrbit(Satellite obj, Graphics g)
	{
		double focus1_x = ((Satellite)obj).orbit.boss.absoluteCurX();
		double focus1_y = ((Satellite)obj).orbit.boss.absoluteCurY();
		
		int focus2_x = drawX(((Satellite)obj).orbit.focus2.getX()+focus1_x);
		int focus2_y = drawY(((Satellite)obj).orbit.focus2.getY()+focus1_y);
		
		focus1_x=drawX(focus1_x);
		focus1_y=drawY(focus1_y);
		
		int x = drawX(((Satellite)(obj)).absoluteCurX());
		int y = drawY(((Satellite)(obj)).absoluteCurY());
		
		if(obj==selected && !game_mode)
		{
			g.setColor(Color.RED);
			g.drawOval(x,y,2,2);
			g.drawOval((int)focus1_x-1,(int)focus1_y-1, 3,3);
			g.drawOval(focus2_x-1, focus2_y-1, 3,3);
		}
		
		double a = ((Satellite)obj).orbit.a*scale;
		double c = ((Satellite)obj).orbit.c*scale;
		double b = ((Satellite)obj).orbit.b*scale;
		
		double theta;
		if(focus2_x != focus1_x)
			theta = Math.atan(((double)(focus2_y-focus1_y))/(focus2_x-focus1_x));
		else if(focus2_y>focus1_y)
			theta=Math.PI/2;
		else if(focus2_y < focus1_y)
			theta=-Math.PI/2;
		else
			theta=0;
		
		Graphics2D g2=(Graphics2D)g;
		g2.rotate(theta);
		
		double center_x=(focus1_x+focus2_x)/2;
		double center_y=(focus1_y+focus2_y)/2;
		
		int start_x=(int)(center_x*Math.cos(theta)+center_y*Math.sin(theta)-a);
		int start_y=(int)(-center_x*Math.sin(theta)+center_y*Math.cos(theta)-b);
		
		if(obj==selected)
			g2.setColor(Color.YELLOW);
		else
			g2.setColor(Color.GRAY);
		g2.drawOval(start_x, start_y, (int)(2*a), (int)(2*b));
		
		g2.rotate(-theta);
	}
}