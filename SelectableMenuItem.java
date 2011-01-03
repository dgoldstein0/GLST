import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

//TODO: remove this class - it is no longer used
@Deprecated
public class SelectableMenuItem extends JMenuItem implements ActionListener
{
	Selectable the_selectable;
	
	public SelectableMenuItem(Selectable s)
	{
		super(s.generateName());
		the_selectable=s;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		GameInterface.GC.GI.selectObjInSystem(the_selectable);
		//GameInterface.GC.GI.select_menu.setVisible(false);
	}
}