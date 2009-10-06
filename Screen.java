import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Screen extends JPanel implements MouseListener
{
	JFrame frame;
	GameControl control;
	
	JLabel pname;
	JLabel dtime;
	JLabel cur_money;
	
	JTabbedPane pface;
	
	JButton search_but;
	JTextField search_box;
	
	public Screen(JFrame frame, GameControl control)
	{
		super();
		
		this.frame=frame;
		this.control=control;
		
		setLayout(new BorderLayout());
		
		JPanel status=new JPanel();
		status.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		pname=new JLabel();
		status.add(pname);
		
		cur_money=new JLabel();
		status.add(cur_money);
		
		dtime=new JLabel();
		status.add(dtime);
		
		add(status, BorderLayout.NORTH);
		status.addMouseListener(this);
		
		pface=new JTabbedPane();
		
		pface.addTab("Buy", new JLabel("Buy Screen"));
		pface.addTab("Sell", new JLabel("Sell Screen"));
		pface.addTab("Services",new JLabel("Service Screen"));//repairs, shipyard, buy/sell parts, banking
		
		JPanel map_pane=new JPanel(new BorderLayout());
		JToolBar search_bar=new JToolBar("Galaxy Search");
		search_bar.setFloatable(false);
		
		search_box=new JTextField();
		search_bar.add(search_box);
		
		search_but=new JButton("Search!");
		search_bar.add(search_but);//needs an actionlistener
		
		map_pane.add(search_bar, BorderLayout.SOUTH);
		
		pface.addTab("Map", map_pane);
		
		pface.setVisible(false);
		
		add(pface, BorderLayout.CENTER);
	}
	
	public void drawStatus()
	{
		pname.setText(control.player.name);
		dtime.setText(Long.toString(control.time_elapsed) + " days");
		cur_money.setText(Long.toString(control.player.money) +" credits");
	}
	
	public void mouseClicked(MouseEvent e)
		{
			if(control instanceof GameControl)
			{
				JDialog game_status=new JDialog(frame, "Game Status", true);
				
				JTabbedPane gtabs=new JTabbedPane();
				
				JPanel g_pane=new JPanel(new GridLayout(4,2));
				
				g_pane.add(new JLabel("Name"));
				g_pane.add(new JLabel(control.player.name));
				
				g_pane.add(new JLabel("Time"));
				g_pane.add(new JLabel(Long.toString(control.time_elapsed)+" days"));
				
				g_pane.add(new JLabel("Money"));
				g_pane.add(new JLabel(Long.toString(control.player.money)+" credits"));
				
				
				JPanel m_pane=new JPanel();
				m_pane.add(g_pane);
				
				gtabs.addTab("Status", m_pane);
				gtabs.addTab("Quests", new JLabel("There are no open quests at the moment."));
				gtabs.addTab("Fleets", new JLabel("Your Fleets should appear here."));
				
				game_status.add(gtabs);
				
				game_status.pack();
				game_status.setVisible(true);
			}
		}
		
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}