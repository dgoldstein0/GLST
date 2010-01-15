import java.awt.*;
import javax.swing.*;
import java.util.*;

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
	
	public SystemPainter(boolean design)
	{
		setMinimumSize(new Dimension(800,600));
		design_view=design;
		ghost_obj=GHOST_NONE;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		setBackground(Color.BLACK);
		
		if(design_view)
		{
			g.setColor(new Color(255,255,0,100));
			g.drawOval(drawX((getWidth()-100)/2),drawY((getHeight()-100)/2),(int)(100*scale),(int)(100*scale));
			
			if(selected instanceof Satellite)
			{
				g.setColor(Color.YELLOW);
				g.drawOval(drawX(((Satellite)selected).absoluteInitX()-((StellarObject)selected).size/2)-2, drawY(((Satellite)selected).absoluteInitY()-((StellarObject)selected).size/2)-2, (int)(((StellarObject)selected).size*scale)+4, (int)(((StellarObject)selected).size*scale)+4);
			}
			else if(selected instanceof Star)
			{
				//select a star
				g.setColor(Color.YELLOW);
				g.drawOval(drawX(((Star)selected).x-((StellarObject)selected).size/2), drawY(((Star)selected).y-((StellarObject)selected).size/2), (int)(((StellarObject)selected).size*scale), (int)(((StellarObject)selected).size*scale));
			}
			else if(selected instanceof Focus)
			{
				g.setColor(Color.YELLOW);
				g.drawOval(drawX(((Focus)selected).getX()+(((Focus)selected).owner.boss.absoluteCurX()))-2, drawY(((Focus)selected).getY()+(((Focus)selected).owner.boss.absoluteCurY()))-2, 5,5);
			}
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
			if(system.orbiting_objects instanceof HashSet)
			{
				for(Satellite orbiting : system.orbiting_objects)
				{
					//draw object
					drawOrbit(orbiting, g);
					g.setColor(Color.WHITE);
					g.fillOval(drawX(orbiting.absoluteCurX()-(orbiting.size/2)), drawY(orbiting.absoluteCurY()-(orbiting.size/2)), (int)(orbiting.size*scale), (int)(orbiting.size*scale));
					
					//draw objects orbiting planets					
					if(orbiting instanceof Planet && ((Planet)orbiting).satellites instanceof HashSet)
					{
						HashSet<Satellite> planet_sats = ((Planet)orbiting).satellites;
						for(Satellite sat : planet_sats)
						{
							drawOrbit(sat, g);
							g.setColor(Color.GRAY);
							g.fillOval(drawX(sat.absoluteCurX()-sat.size/2), drawY(sat.absoluteCurY()-sat.size/2), (int)(sat.size*scale), (int)(sat.size*scale));
						}
					}
				}
			}
		}
		
		if(ghost_obj==GHOST_OBJ)
		{
			g.setColor(Color.GRAY);
			g.drawOval(drawX(ghost_x-ghost_size/2), drawY(ghost_y-ghost_size/2), (int)(ghost_size*scale), (int)(ghost_size*scale));
		}
		
		
		g.setColor(Color.RED);
		g.drawLine(drawX(center_x-5),drawY(center_y),drawX(center_x+5),drawY(center_y));
		g.drawLine(drawX(center_x),drawY(center_y-5),drawX(center_x),drawY(center_y+5));
	}
	
	public void paintSystem(GSystem system, Selectable selected, boolean view, int centerx, int centery, double sc)
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
	
	public void paintSystem(GSystem system, Selectable selected, int centerx, int centery, double sc)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_NONE;
		center_x=centerx;
		center_y=centery;
		scale=sc;
		repaint();
	}
	
	public void paintGhostObj(GSystem system, Selectable selected, int x, int y, int size, int centerx, int centery, double sc)
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
		return (int)((the_x-center_x)*scale+((double)getWidth())/2.0d);
	}
	
	public int drawY(double the_y)
	{
		return (int)((the_y-center_y)*scale+((double)getHeight())/2.0d);
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
		
		if(obj==selected)
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