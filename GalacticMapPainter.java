import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;

public class GalacticMapPainter extends JPanel
{
	Galaxy map;
	HashSet<GSystem> selected;
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
		setBackground(Color.BLACK);
		
		if(map != null && map.systems != null)
		{
			for(GSystem sys : map.systems)
			{
				if(sys.navigability>=nav_level || display_unnavigable)
				{
					if(sys.navigability < nav_level)
						g.setColor(Color.GRAY);
					else if(sys.owner_id == GSystem.NO_OWNER)
						g.setColor(Color.WHITE);
					else if(sys.owner_id == GSystem.OWNER_CONFLICTED)
						g.setColor(Color.ORANGE);
					else
						g.setColor(GameInterface.GC.players[sys.owner_id].getColor());
					g.fillOval(scaleNum(sys.x-2),scaleNum(sys.y-2),scaleNum(5),scaleNum(5));
					if(nav_display == GDFrame.NAV_DISP_ALL)
					{
						g.setFont(g.getFont().deriveFont(Font.BOLD,12.0f));
						FontMetrics m=g.getFontMetrics(g.getFont());
						g.setColor(Color.WHITE);
						g.drawString(Integer.toString(sys.navigability), scaleNum(sys.x+3), scaleNum(sys.y)+m.getHeight());
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
									
									g.setColor(Color.RED);
									g.drawLine(scaleNum(sel_sys.x), scaleNum(sel_sys.y), scaleNum(sys.x), scaleNum(sys.y));
									
									g.setFont(g.getFont().deriveFont(Font.BOLD,11.0f));
									FontMetrics m=g.getFontMetrics(g.getFont());
									g.setColor(Color.WHITE);
									g.drawString(dist,scaleNum((sel_sys.x+sys.x)/2)-m.stringWidth(dist)/2,scaleNum((sel_sys.y+sys.y)/2)+m.getHeight()/2);
								}
							}
						}
					}
				}
			}
			
			if(selected instanceof HashSet)
			{
				for(GSystem sys : selected)
				{
					g.setColor(Color.ORANGE);
					g.drawOval(scaleNum(sys.x-4),scaleNum(sys.y-4),scaleNum(9),scaleNum(9));
					g.setColor(Color.WHITE);
					
					g.setFont(g.getFont().deriveFont(Font.BOLD,12.0f));
					FontMetrics m=g.getFontMetrics(g.getFont());
					
					if(nav_display == GDFrame.NAV_DISP_SELECTED)
					{
						g.setColor(Color.WHITE);
						g.drawString(Integer.toString(sys.navigability), scaleNum(sys.x+3), scaleNum(sys.y)+m.getHeight());
					}
					else if(disp_names)
					{
						if(sys.name instanceof String) {
							if(sys.navigability < nav_level)
								g.setColor(Color.GRAY);
							else if(sys.owner_id == GSystem.NO_OWNER)
								g.setColor(Color.WHITE);
							else if(sys.owner_id == GSystem.OWNER_CONFLICTED)
								g.setColor(Color.ORANGE);
							else
								g.setColor(GameInterface.GC.players[sys.owner_id].getColor());
							g.drawString(sys.name, scaleNum(sys.x+3), scaleNum(sys.y)+m.getHeight());
						} else {
							g.setColor(Color.YELLOW);
							g.drawString("Unnamed", scaleNum(sys.x+3), scaleNum(sys.y)+m.getHeight());
						}
					}
				}
			}
			
			if(drag_options == GDFrame.DRAG_RANGE)
			{
				for(GSystem sel_sys : selected)
				{
					g.setColor(Color.GREEN);
					g.drawOval(scaleNum(sel_sys.x-max_dist_shown),scaleNum(sel_sys.y-max_dist_shown),scaleNum(2*max_dist_shown),scaleNum(2*max_dist_shown));
				}
			}
			
			if(ships_in_transit != null)
			{
				Graphics2D g2 = (Graphics2D)g;
				for(Ship s : ships_in_transit)
				{
					g2.setColor(s.owner.getColor());
					
					//set up coordinates of equilateral triangle
					double side = 10.0;
					double h = side/2.0*Math.sqrt(3);
					int[] xcoords = {(int)(-side/2.0), (int)(side/2.0), 0};
					int[] ycoords = {(int)(-h/3.0),(int)(-h/3.0),(int)(2.0/3.0*h)};
					
					// Get the current transform
					AffineTransform saveAT = g2.getTransform();
					
					//draw ship s
					g2.rotate(s.exit_direction, s.getPos_x(),s.getPos_y());
					g2.drawPolygon(xcoords, ycoords,3);
					g2.setTransform(saveAT);
				}
			}
		}
		
		if(select_box)
		{
			g.setColor(Color.GRAY);
			g.drawRect(scaleNum(select_box_x1), scaleNum(select_box_y1), scaleNum(select_box_x2-select_box_x1), scaleNum(select_box_y2-select_box_y1));
		}
		else if(ghost_system)
		{
			g.setColor(Color.GRAY);
			g.fillOval(scaleNum(ghost_x-2),scaleNum(ghost_y-2),scaleNum(5),scaleNum(5));
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(0,0,scaleNum(GalacticStrategyConstants.GALAXY_WIDTH), scaleNum(GalacticStrategyConstants.GALAXY_HEIGHT));
	}
	
	public void paintGalaxy(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav, ArrayList<Ship> transit, double sc)
	{
		this.map=map;
		this.selected=selected;
		select_box=false;
		ghost_system=false;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		ships_in_transit = transit;
		
		if(!(selected instanceof HashSet))
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
		
		if(!(selected instanceof HashSet))
			drag_options=GDFrame.DRAG_NONE;
		else
			drag_options=options;
		
		scale=sc;
		
		repaint();
	}
	
	private int scaleNum(int x)
	{
		return (int)(((double)x)*scale);
	}
}