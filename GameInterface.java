import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class GameInterface implements Runnable, ActionListener, ChangeListener, MouseMotionListener, MouseListener, WindowListener
{
	
	JFrame frame;
	JPanel panel;
	JTextField time,resource;
	SpringLayout layout;

	public GameInterface()
	{
		layout=new SpringLayout();
		frame=new JFrame("Galactic Strategy Game");
		frame.setLayout(layout);				
		resource=new JTextField("Resource: 0");
		time=new JTextField("Time: 0");		
		frame.add(time);
		frame.add(resource);		      
	}	
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new GameInterface());
	}
	
	public void run()
	{
		frame.setVisible(true);
		frame.setSize(1200, 900);
		layout.putConstraint(SpringLayout.WEST, resource,
                5,
                SpringLayout.WEST, frame);
        layout.putConstraint(SpringLayout.NORTH, resource,
                5,
                SpringLayout.NORTH, frame);
		layout.putConstraint(SpringLayout.EAST, time,
                900,
                SpringLayout.EAST, frame);
        layout.putConstraint(SpringLayout.NORTH, time,
                5,
                SpringLayout.NORTH, frame);  
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
