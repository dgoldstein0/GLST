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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.FlowLayout;


public class GameInterface implements Runnable, ActionListener, ChangeListener, MouseMotionListener, MouseListener, WindowListener
{
	
	JFrame frame;
	JPanel panel,topbar;
	JLabel time,resource;	
	JButton menubutton;
	JPopupMenu menu;
	JTabbedPane tabbedPane;
	GamingInterface theinterface;
	JTextArea log;
	JPanel stat_and_order;
	
	final String indentation="     ";


	public GameInterface()
	{
		//create frame and layout	
		frame=new JFrame("Galactic Strategy Game");
		//frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		frame.setVisible(true);
		frame.setSize(1500,900);
		frame.setMinimumSize(new Dimension(800,600));
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
		

	    //Create the popup menu.
		menubutton=new JButton("menu");
		ActionListener actionListener = new ActionListener() {
		  public void actionPerformed(ActionEvent actionEvent) {
		    	  menu=new JPopupMenu("menu");
		    	  Runnable showDialog = new Runnable() {
		    		  public void run() {
		    			   menu.show();
		    		  }
		    	  };
          SwingUtilities.invokeLater(showDialog);
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
		theinterface=new GamingInterface();
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
	
	public void run()
	{
		//set the frame

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
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

}
