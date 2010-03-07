import javax.swing.*;
import java.awt.event.*;

public class ShipCommandPanel extends JPanel implements ActionListener
{
	Ship the_ship;
	JButton move;
	
	
	public ShipCommandPanel()
	{
		super();
		
		move=new JButton("Move");
		move.addActionListener(this);
		add(move);
	}
	
	public void setShip(Ship s)
	{
		the_ship=s;
	}
	
	public void actionPerformed(ActionEvent e)
	{
	}
}