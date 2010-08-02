import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class GalacticMapPainter extends JPanel
{
	Galaxy map;
	Set<GSystem> selected;
	ArrayList<Ship> ships_in_transit;
	int drag_options;
	int max_dist_shown;
	int nav_level;
	int nav_display; //the number 0, 1, or 2 specifying which navigabilities to display
	boolean display_unnavigable;
	boolean disp_names=true;
	
	boolean select_box;
	int select_box_x1;
	int select_box_y1;
	int select_box_x2;
	int select_box_y2;
	
	boolean ghost_system;
	int ghost_x;
	int ghost_y;
	
	double scale;
	
	public GalacticMapPainter()
	{
		super(new FlowLayout(FlowLayout.LEFT));
		drag_options=GDFrame.DRAG_NONE;
		max_dist_shown=GalacticStrategyConstants.DEFAULT_DIST;
		nav_level=GalacticStrategyConstants.DEFAULT_NAV_LEVEL;
		scale=1.0d;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		long time = (GameInterface.GC != null) ? GameInterface.GC.TC.getTime() : 0;
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		setBackground(Color.BLACK);
		
		if(map != null && map.systems != null)
		{
			for(GSystem sys : map.systems)
			{
				if(sys.navigability>=nav_level || display_unnavigable)
				{
					if(sys.navigability < nav_level)
						g2.setColor(Color.GRAY);
					else
						g2.setColor(sys.currentColor(time));
					g2.fill(new Ellipse2D.Double(scaleNum(sys.x-2),scaleNum(sys.y-2),scaleNum(5),scaleNum(5)));
					if(nav_display == GDFrame.NAV_DISP_ALL)
					{
						g2.setFont(g2.getFont().deriveFont(Font.BOLD,12.0f));
						FontMetrics m=g2.getFontMetrics(g2.getFont());
						g2.setColor(Color.WHITE);
						g2.drawString(Integer.toString(sys.navigability), (float)scaleNum(sys.x+3), (float)scaleNum(sys.y)+m.getHeight());
					}
					
					if(selected != null)
					{
						for(GSystem sel_sys : selected)
						{
							if(sel_sys != sys && drag_options == GDFrame.DRAG_DIST)
							{
								double d=Math.hypot(sys.x-sel_sys.x,sys.y-sel_sys.y);
								if(((int)d) <= max_dist_shown)
								{
									String dist=Integer.toString((int)d);
									
									g2.setColor(Color.RED);
									g2.draw(new Line2D.Double(scaleNum(sel_sys.x), scaleNum(sel_sys.y), scaleNum(sys.x), scaleNum(sys.y)));
									
									g2.setFont(g2.getFont().deriveFont(Font.BOLD,11.0f));
									FontMetrics m=g2.getFontMetrics(g2.getFont());
									g2.setColor(Color.WHITE);
									g2.drawString(dist,(float)(scaleNum((sel_sys.x+sys.x)/2.0)-m.stringWidth(dist)/2.0),(float)(scaleNum((sel_sys.y+sys.y)/2.0)+m.getHeight()/2.0));
								}
							}
						}
					}
				}
			}
			
			if(selected != null)
			{
				for(GSystem sys : selected)
				{
					g2.setColor(Color.ORANGE);
					g2.draw(new Ellipse2D.Double(scaleNum(sys.x-4),scaleNum(sys.y-4),scaleNum(9),scaleNum(9)));
					g2.setColor(Color.WHITE);
					
					g2.setFont(g2.getFont().deriveFont(Font.BOLD,12.0f));
					FontMetrics m=g2.getFontMetrics(g2.getFont());
					
					if(nav_display == GDFrame.NAV_DISP_SELECTED)
					{
						g2.setColor(Color.WHITE);
						g2.drawString(Integer.toString(sys.navigability), (float)scaleNum(sys.x+3), (float)scaleNum(sys.y)+m.getHeight());
					}
					else if(disp_names)
					{
						if(sys.name instanceof String) {
							if(sys.navigability < nav_level)
								g2.setColor(Color.GRAY);
							else
								g2.setColor(sys.currentColor(time));
							g2.drawString(sys.name, (float)scaleNum(sys.x+3), (float)scaleNum(sys.y)+m.getHeight());
						} else {
							g2.setColor(Color.YELLOW);
							g2.drawString("Unnamed", (float)scaleNum(sys.x+3), (float)scaleNum(sys.y)+m.getHeight());
						}
					}
				}
			}
			
			if(drag_options == GDFrame.DRAG_RANGE)
			{
				for(GSystem sel_sys : selected)
				{
					g2.setColor(Color.GREEN);
					g2.draw(new Ellipse2D.Double(scaleNum(sel_sys.x-max_dist_shown),scaleNum(sel_sys.y-max_dist_shown),scaleNum(2*max_dist_shown),scaleNum(2*max_dist_shown)));
				}
			}
			
			if(ships_in_transit != null)
			{
				g2.setColor(GameInterface.GC.players[GameInterface.GC.player_id].getColor());
				for(Ship s : ships_in_transit)
				{
					//set up coordinates of equalateral triangle
					double side = 10.0;
					double h = side/2.0*Math.sqrt(3.0);
					double scale_x = scaleNum(s.getPos_x());
					double scale_y = scaleNum(s.getPos_y());
					
					double[] xcoords = {-side/2.0 + scale_x,	side/2.0 + scale_x,	scale_x};
					double[] ycoords = {-h/3.0 + scale_y,		-h/3.0+scale_y,		2.0/3.0*h+scale_y};
					
					Path2D.Double triangle = new Path2D.Double();
					triangle.moveTo(xcoords[0], ycoords[0]);
					triangle.lineTo(xcoords[1], ycoords[1]);
					triangle.lineTo(xcoords[2], ycoords[2]);
					triangle.lineTo(xcoords[0], ycoords[0]);
					
					// Get the current transform
					AffineTransform saveAT = g2.getTransform();
					
					//draw ship s
					g2.rotate(s.exit_direction-Math.PI/2, scale_x, scale_y);
					g2.draw(triangle);
					g2.setTransform(saveAT);
				}
			}
		}
		
		if(select_box)
		{
			g2.setColor(Color.GRAY);
			g2.draw(new Rectangle2D.Double(scaleNum(select_box_x1), scaleNum(select_box_y1), scaleNum(select_box_x2-select_box_x1), scaleNum(select_box_y2-select_box_y1)));
		}
		else if(ghost_system)
		{
			g2.setColor(Color.GRAY);
			g2.fill(new Ellipse2D.Double(scaleNum(ghost_x-2),scaleNum(ghost_y-2),scaleNum(5),scaleNum(5)));
		}
		
		g2.setColor(Color.WHITE);
		g2.draw(new Rectangle2D.Double(0,0,scaleNum(GalacticStrategyConstants.GALAXY_WIDTH), scaleNum(GalacticStrategyConstants.GALAXY_HEIGHT)));
	}
	
	public void paintGalaxy(Galaxy map, Set<GSystem> selectedSys, int options, int nav, int disp_nav, boolean unnav, ArrayList<Ship> transit, double sc)
	{
		this.map=map;
		this.selected=selectedSys;
		select_box=false;
		ghost_system=false;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		ships_in_transit = transit;
		
		if(selectedSys == null)
			drag_options=GDFrame.DRAG_NONE;
		else
			drag_options=options;
		
		scale=sc;
		
		repaint();
	}
	
	public void paintSelect(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav, ArrayList<Ship> transit, int x1, int y1, int x2, int y2, double sc)
	{
		this.map=map;
		this.selected=selected;
		drag_options=options;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		ships_in_transit = transit;
		
		select_box_x1=x1;
		select_box_y1=y1;
		
		select_box_x2=x2;
		select_box_y2=y2;
		
		select_box=true;
		ghost_system=false;
		
		scale=sc;
		
		repaint();
	}
	
	public void paintGhostSystem(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav, ArrayList<Ship> transit, int ghost_x, int ghost_y, double sc)
	{
		this.map=map;
		this.selected=selected;
		select_box=false;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		ships_in_transit = transit;
		
		ghost_system=true;
		this.ghost_x=ghost_x;
		this.ghost_y=ghost_y;
		
		if(selected == null)
			drag_options=GDFrame.DRAG_NONE;
		else
			drag_options=options;
		
		scale=sc;
		
		repaint();
	}
	
	public void setMaxDistShown(int dist)
	{
		max_dist_shown=dist;
	}
	
	private double scaleNum(int x)
	{
		return ((double)x)*scale;
	}
	
	private double scaleNum(double x)
	{
		return x*scale;
	}
}