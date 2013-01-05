import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


public class KeyboardHandler {

	boolean enabled;
	
	public KeyboardHandler(JComponent comp)
	{
		enabled=true;

		InputMap inmap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actmap = comp.getActionMap();
		for(int i=1; i<=9; ++i)
		{
			inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1-1+i, InputEvent.CTRL_DOWN_MASK), i);
			inmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1-1+i, 0), i);
			actmap.put(i, new GroupHandler());
		}
	}
	
	public void setEnabled(boolean b) {
		
		enabled=b;
	}
	
	private class GroupHandler implements Action
	{
		final List<Selectable> group;

		private GroupHandler()
		{
			group = new ArrayList<Selectable>();
		}
		
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void putValue(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setEnabled(boolean b) {
			enabled=b;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
			{
				group.clear();
				group.addAll(GameInterface.GC.GI.selected_in_sys);
			}
			else
			{
				GameInterface.GC.GI.selected_in_sys.clear();
				GameInterface.GC.GI.selected_in_sys.addAll(group);
				GameInterface.GC.GI.redraw();
			}
		}
	}
}
