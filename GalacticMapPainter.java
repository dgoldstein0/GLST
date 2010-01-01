import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GalacticMapPainter extends JPanel
{
	Galaxy map;
	HashSet<GSystem> selected;
	int drag_options;
	int max_dist_shown;
	int nav_level;
	int nav_display; //the number 0, 1, or 2 specifying which navigabilities to display
	boolean display_unnavigable;
	
	boolean select_box;
	int select_box_x1;
	int select_box_y1;
	int select_box_x2;
	int select_box_y2;
	
	boolean ghost_system;
	int ghost_x;
	int ghost_y;
	
	public GalacticMapPainter()
	{
		super();
		setMinimumSize(new Dimension(800,600));
		setPreferredSize(new Dimension(800,600));
		setMaximumSize(new Dimension(800,600));
		drag_options=GDFrame.DRAG_NONE;
		max_dist_shown=GalacticStrategyConstants.DEFAULT_DIST;
		nav_level=GalacticStrategyConstants.DEFAULT_NAV_LEVEL;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		setBackground(Color.BLACK);
		
		if(map instanceof Galaxy && map.systems instanceof HashSet)
		{
			g.setColor(Color.WHITE);
			for(GSystem sys : map.systems)
			{
				if(sys.navigability>=nav_level || display_unnavigable)
				{
					if(sys.navigability < nav_level)
						g.setColor(Color.GRAY);
					else
						g.setColor(Color.WHITE);
					g.fillOval(sys.x-2,sys.y-2,5,5);
					if(nav_display == GDFrame.NAV_DISP_ALL)
					{
						g.setFont(g.getFont().deriveFont(Font.BOLD,12.0f));
						FontMetrics m=g.getFontMetrics(g.getFont());
						g.setColor(Color.WHITE);
						g.drawString(Integer.toString(sys.navigability), sys.x+3, sys.y+m.getHeight());
					}
					
					if(selected instanceof HashSet)
					{
						for(GSystem sel_sys : selected)
						{
							if(sel_sys != sys && drag_options == GDFrame.DRAG_DIST)
							{
								double d=Math.hypot(sys.x-sel_sys.x,sys.y-sel_sys.y);
								if(d<=max_dist_shown)
								{
									String dist=Integer.toString((int)d);
									
									g.setColor(Color.RED);
									g.drawLine(sel_sys.x, sel_sys.y, sys.x, sys.y);
									
									g.setFont(g.getFont().deriveFont(Font.BOLD,11.0f));
									FontMetrics m=g.getFontMetrics(g.getFont());
									g.setColor(Color.WHITE);
									g.drawString(dist,(sel_sys.x+sys.x)/2-m.stringWidth(dist)/2,(sel_sys.y+sys.y)/2+m.getHeight()/2);
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
					g.drawOval(sys.x-4,sys.y-4,9,9);
					g.setColor(Color.WHITE);
					if(nav_display == GDFrame.NAV_DISP_SELECTED)
					{
						g.setFont(g.getFont().deriveFont(Font.BOLD,12.0f));
						FontMetrics m=g.getFontMetrics(g.getFont());
						g.setColor(Color.WHITE);
						g.drawString(Integer.toString(sys.navigability), sys.x+3, sys.y+m.getHeight());
					}
				}
			}
			
			if(drag_options == GDFrame.DRAG_RANGE)
			{
				for(GSystem sel_sys : selected)
				{
					g.setColor(Color.GREEN);
					g.drawOval(sel_sys.x-max_dist_shown,sel_sys.y-max_dist_shown,2*max_dist_shown,2*max_dist_shown);
				}
			}
			
			if(select_box)
			{
				g.setColor(Color.GRAY);
				g.drawRect(select_box_x1, select_box_y1, select_box_x2-select_box_x1, select_box_y2-select_box_y1);
			}
			else if(ghost_system)
			{
				g.setColor(Color.GRAY);
				g.fillOval(ghost_x-2,ghost_y-2,5,5);
			}
		}
	}
	
	public void paintGalaxy(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav)
	{
		this.map=map;
		this.selected=selected;
		select_box=false;
		ghost_system=false;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		
		if(!(selected instanceof HashSet))
			drag_options=GDFrame.DRAG_NONE;
		else
			drag_options=options;
		
		repaint();
	}
	
	public void paintSelect(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav, int x1, int y1, int x2, int y2)
	{
		this.map=map;
		this.selected=selected;
		drag_options=options;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		
		select_box_x1=x1;
		select_box_y1=y1;
		
		select_box_x2=x2;
		select_box_y2=y2;
		
		select_box=true;
		ghost_system=false;
		
		repaint();
	}
	
	public void paintGhostSystem(Galaxy map, HashSet<GSystem> selected, int options, int nav, int disp_nav, boolean unnav, int ghost_x, int ghost_y)
	{
		this.map=map;
		this.selected=selected;
		select_box=false;
		nav_level=nav;
		nav_display=disp_nav;
		display_unnavigable=unnav;
		
		ghost_system=true;
		this.ghost_x=ghost_x;
		this.ghost_y=ghost_y;
		
		if(!(selected instanceof HashSet))
			drag_options=GDFrame.DRAG_NONE;
		else
			drag_options=options;
		
		repaint();
	}
}