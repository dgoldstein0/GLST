import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.BorderLayout;


public class GameInterface implements Runnable, ActionListener, MouseMotionListener, MouseListener, WindowListener, ComponentListener
{
	
	JFrame frame;
	JPanel panel,topbar;
	JLabel time,resource;	
	JButton menubutton;
	GameMenu menu;
	JTabbedPane tabbedPane;
	JTextArea log;
	JPanel stat_and_order;
	JPanel theinterface;
	
	GalacticMapPainter GalaxyPanel;
	SystemPainter SystemPanel;
	
	GameControl GC;
	GSystem sys;
	Selectable selected_in_sys;
	
	boolean mode; //Galaxy=true, system=false.  reflected by isGalaxyDisplayed and isSystemDisplayed
	boolean graphics_started; //used to indicate whether graphics have been started yet - that is, whether the Galaxy has been drawn yet.
	
	final String indentation="     ";


	public GameInterface()
	{
		//create frame and layout	
		frame=new JFrame("Galactic Strategy Game");
		menu=new GameMenu(frame);
		//frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		frame.setSize(1500,900);
		frame.setMinimumSize(new Dimension(800,600));
		frame.addWindowListener(this);
		frame.addComponentListener(this);
		
		panel= new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		frame.add(panel);
		
		//create topbar
		topbar=new JPanel(new GridBagLayout());
		//create resource		
		resource=new JLabel(indentation+"Resource: 0");
	//	resource.setSize(600, 200);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =0.4;
		c.weighty=0; 
		c.gridx = 0;
		c.gridy = 0;
		//c.gridwidth=2;
		c.anchor=GridBagConstraints.NORTH;
		c.gridheight=1;
		topbar.add(resource,c);
						
		//create time
		time=new JLabel("Time: 0");
	//	time.setSize(600, 200);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =0.4;
		c.weighty=0;
		c.gridx = 10;
		c.gridy = 0;
		//c.gridwidth=2;
		topbar.add(time,c);
		
		//add topbar
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.weightx=1;
		c.weighty=0.0;		
		c.gridx=0;
		c.gridy=0;
		topbar.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(topbar,c);
		

		//Create the menu button
		menubutton=new JButton("menu");
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				menu.showMenu();
			}
		};
		menubutton.addActionListener(actionListener);
		menubutton.setSize(300, 100);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.weightx =0.41;
		c.gridx = 15;
		c.gridy = 0;    
		topbar.add(menubutton,c);
		

		
		//create the tabbed pane
		tabbedPane = new JTabbedPane();		
		//tabbedPane.setSize(200, 700);
		Component pane1 = makeTextPanel("System");
		tabbedPane.addTab("System", pane1);
		tabbedPane.setSelectedIndex(0);
		Component pane2 = makeTextPanel("Planets");
		tabbedPane.addTab("Planets", pane2);
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.EAST;
		c.weightx =0.2;
		c.weighty=0.8;
		c.gridwidth=5;
		c.gridx = 15;
		c.gridy = 1;    
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(tabbedPane,c);


		//create the interface
		theinterface = new JPanel(new BorderLayout());
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.CENTER;
		c.weightx =0.5;
		c.weighty=0.5;
		c.gridx = 0;
		c.gridy = 1;    
		theinterface.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		panel.add(theinterface,c);
		
		//create the chat and information log
		log=new JTextArea("log");
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.SOUTHEAST;
		c.weightx =0.2;
		c.weighty=0.2;
		c.gridx = 15;
		c.gridy = 2;   
		panel.add(log,c);
		
		//create the stat and order panel
		stat_and_order=new JPanel();
		c.fill=GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.SOUTHWEST;
		c.weighty=0;
		c.weightx=1;
		c.gridx=0;
		c.gridy=2;
		stat_and_order.setBorder(BorderFactory.createLineBorder(Color.RED));
		panel.add(stat_and_order,c);	
	
		setupGraphics();
	
		frame.pack();
		
		//set up game control
		GC = new GameControl(this);
		
		frame.setVisible(true);	
		GC.startupDialog();
	}	
	
	//for the tabbed pane
    protected Component makeTextPanel (String text) 
    {
        JPanel apanel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        apanel.setLayout(new GridLayout(1, 1));
        apanel.add(filler);
        return apanel;
    }
    
    
	

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new GameInterface());
	}
	
	private void setupGraphics()
	{
		//sets up GalacticMapPainter, SystemPainter
		GalaxyPanel = new GalacticMapPainter();
		SystemPanel = new SystemPainter(false);
		graphics_started=false;
	}
	
	public void drawGalaxy()
	{
		System.out.println("Draw Galaxy!");
		//this method shows the GalacticMapPainter in the main viewspace
		if(isSystemDisplayed() || !graphics_started)
		{
			theinterface.removeAll();
			theinterface.add(GalaxyPanel); //automatically adds to center
			mode=true;
			graphics_started=true;
		}
		double scale =  Math.min(((double)theinterface.getWidth())/((double)GalacticStrategyConstants.GALAXY_WIDTH), ((double)theinterface.getHeight())/((double)GalacticStrategyConstants.GALAXY_HEIGHT));
		GalaxyPanel.paintGalaxy(GC.map, null, GDFrame.DRAG_NONE, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, scale);
		frame.pack();
	}
	
	
	//before calling this function, sys and selected_in_sys should be specified.  the latter may be null.
	public void drawSystem()
	{
		System.out.println("Draw System!");
		//this method shows the GalacticMapPainter in the main viewspace
		if(isGalaxyDisplayed())
		{
			theinterface.removeAll();
			theinterface.add(SystemPanel); //automatically adds to center
			mode=false;
		}
		SystemPanel.paintSystem(sys, selected_in_sys, theinterface.getWidth()/2, theinterface.getHeight()/2, 1.0d);
		frame.pack();
	}
	
	public void redraw()
	{
		if(graphics_started)
		{
			if(isGalaxyDisplayed())
			{
				double scale =  Math.min(((double)theinterface.getWidth())/((double)GalacticStrategyConstants.GALAXY_WIDTH), ((double)theinterface.getHeight())/((double)GalacticStrategyConstants.GALAXY_HEIGHT));
				System.out.println(Double.toString(scale));
				GalaxyPanel.paintGalaxy(GC.map, null, GDFrame.DRAG_NONE, GalacticStrategyConstants.MAX_NAV_LEVEL, GDFrame.NAV_DISP_NONE, false, scale);
			}
			else //before getting to here, sys and selected_in_sys should be specified.  the latter may be null.
			{
				SystemPanel.paintSystem(sys, selected_in_sys, theinterface.getWidth()/2, theinterface.getHeight()/2, 1.0d);
			}
		}
	}
	
	public boolean isGalaxyDisplayed(){return mode;}
	public boolean isSystemDisplayed(){return !mode;}
	
	public void run()
	{
		//set the frame

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		GC.endAllThreads(); //used to end any connections, and notify other players, in addition to closing errant threads.
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentShown(ComponentEvent e){}
	
	public void componentResized(ComponentEvent e)
	{
		redraw();
	}
}