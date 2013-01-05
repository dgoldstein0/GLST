import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleNotifier extends JPanel
{
	private JLabel msg_label;
	private Timer t;
	private HideTask task;
	
	public SimpleNotifier()
	{
		msg_label = new JLabel();
		add(msg_label);
		
		setMinimumSize(new Dimension(0,0));
		//setMaximumSize(new Dimension(300,75));
		setBackground(new Color(200,0,0));
		
		setBorder(BorderFactory.createLineBorder(new Color(100,0,0),2));
		setVisible(false);
		t=new Timer(true); //the true makes the timer daemon - that is, the timer will be terminated when the program is shut down
	}
	
	public void showMessage(String msg)
	{
		//msg_color = clr;
		setVisible(true);
		msg_label.setForeground(Color.WHITE);
		msg_label.setText(msg);
		//setPreferredSize(msg_label.getPreferredSize());
		
		if(task != null)
			task.cancel();
		task=new HideTask();
		t.schedule(task, 3000);
	}
	
	public class HideTask extends TimerTask
	{
		public void run()
		{
			SwingUtilities.invokeLater(new Runnable(){public void run(){hideMessage();}}); //invokeLater is necessary since swing is not thread safe
		}
	}
	
	public void hideMessage()
	{
		setVisible(false);
	}
}