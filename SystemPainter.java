import java.awt.*;
import javax.swing.*;
import java.util.*;

public class SystemPainter extends JPanel
{
	final int GHOST_NONE=0;
	final int GHOST_OBJ=1;
	
	GSystem system;
	StellarObject selected;
	
	boolean design_view;
	int focus2_x;
	int focus2_y;
	int x;
	int y;
	
	int ghost_obj;
	int ghost_x;
	int ghost_y;
	int ghost_size;
	
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
			g.drawOval((getWidth()-100)/2,(getHeight()-100)/2,100,100);
			
			if(selected instanceof Satellite)
			{
				g.setColor(Color.YELLOW);
				g.drawOval(((Satellite)selected).absoluteInitX()-selected.size/2-2, ((Satellite)selected).absoluteInitY()-selected.size/2-2, selected.size+4, selected.size+4);
			}
			else if(selected instanceof Star)
			{
				//select a star
				g.setColor(Color.YELLOW);
				g.drawOval(((Star)selected).x-selected.size/2, ((Star)selected).y-selected.size/2, selected.size, selected.size);
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
					Image star_img = Toolkit.getDefaultToolkit().getImage(Star.color_choice[st.color]);
					g.drawImage(star_img, st.x-(st.size/2), st.y-(st.size/2), st.size, st.size, this);
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
					g.fillOval(orbiting.absoluteCurX()-(orbiting.size/2), orbiting.absoluteCurY()-(orbiting.size/2), orbiting.size, orbiting.size);
					
					//draw objects orbiting planets					
					if(orbiting instanceof Planet && ((Planet)orbiting).satellites instanceof HashSet)
					{
						HashSet<Satellite> planet_sats = ((Planet)orbiting).satellites;
						for(Satellite sat : planet_sats)
						{
							drawOrbit(sat, g);
							g.setColor(Color.GRAY);
							g.fillOval(sat.absoluteCurX()-sat.size/2, sat.absoluteCurY()-sat.size/2, sat.size, sat.size);
						}
					}
				}
			}
		}
		
		if(ghost_obj==GHOST_OBJ)
		{
			g.setColor(Color.GRAY);
			g.drawOval(ghost_x-ghost_size/2, ghost_y-ghost_size/2, ghost_size, ghost_size);
		}
	}
	
	public void paintSystem(GSystem system, StellarObject selected, boolean view)
	{
		this.system=system;
		this.selected=selected;
		design_view=view;
		ghost_obj=GHOST_NONE;
		repaint();
	}
	
	public void paintSystem(GSystem system, StellarObject selected)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_NONE;
		repaint();
	}
	
	public void paintGhostObj(GSystem system, StellarObject selected, int x, int y, int size)
	{
		this.system=system;
		this.selected=selected;
		ghost_obj=GHOST_OBJ;
		ghost_x=x;
		ghost_y=y;
		ghost_size=size;
		repaint();
	}
	
	private void drawOrbit(Satellite obj, Graphics g)
	{
		int focus1_x = ((Satellite)obj).orbit.boss.absoluteCurX();
		int focus1_y = ((Satellite)obj).orbit.boss.absoluteCurY();
		
		int focus2_x = ((Satellite)obj).orbit.focus2_x+focus1_x;
		int focus2_y = ((Satellite)obj).orbit.focus2_y+focus1_y;
		
		int x = ((Satellite)(obj)).absoluteCurX();
		int y = ((Satellite)(obj)).absoluteCurY();
		
		if(obj==selected)
		{
			g.setColor(Color.RED);
			g.drawOval(x,y,2,2);
			g.drawOval(focus1_x-1,focus1_y-1, 3,3);
			g.drawOval(focus2_x-1, focus2_y-1, 3,3);
		}
		
		double a = ((Satellite)obj).orbit.a;
		double c = ((Satellite)obj).orbit.c;
		double b = ((Satellite)obj).orbit.b;
		
		double theta = Math.atan(((double)(focus2_y-focus1_y))/(focus2_x-focus1_x));
		
		Graphics2D g2=(Graphics2D)g;
		g2.rotate(theta);
		
		int center_x=(focus1_x+focus2_x)/2;
		int center_y=(focus1_y+focus2_y)/2;
		
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