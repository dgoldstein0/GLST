import java.awt.event.KeyEvent;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

public class UndoRedoStack
{
	static final int STACK_SIZE=21;
	
	//this should be Galaxies encoded by XMLEncoder and decoded by XMLDecoder.  encoding ensures that data, and not pointers, are saved
	private String[] stack; //newest in first position, oldest in last position
	private int current;
	private int num_objs;
	
	//isCtrlZ should be checked before calling UNDO
	//must be called by the KeyTyped() from KeyListener
	public static boolean isCtrlZ(KeyEvent e) throws IllegalArgumentException
	{
		if(e.getID() != KeyEvent.KEY_RELEASED)
			throw new IllegalArgumentException();
		//System.out.println(e.getKeyChar());
		
		return (e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown());
	}
	
	//isCtrlY should be checked before calling REDO
	public static boolean isCtrlY(KeyEvent e) throws IllegalArgumentException
	{
		if(e.getID() != KeyEvent.KEY_RELEASED)
			throw new IllegalArgumentException();
		return (e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown());
	}
	
	public UndoRedoStack(Object[] obj)
	{
		stack=new String[STACK_SIZE];
		stack[0]=ObjsToXML(obj);
		current=0;
		num_objs=obj.length;
	}
	
	public void objectsChanged(Object[] obj) throws IllegalArgumentException
	{
		/*This method takes the new object, places it first in the stack, then places what was current and everything after that after the new object in the stack.
		*/
		//note that the end of the stack may be lost here.  Things that are saved for possible redos are thrown away.

		if(obj.length != num_objs)
			throw new IllegalArgumentException();
		
		String[] temp_stack = new String[STACK_SIZE+1]; //temporary storage to facilitate shifting of the array
		temp_stack[0]=ObjsToXML(obj);
		
		for(int i=0; i<STACK_SIZE-current; i++)
		{
			temp_stack[i+1]=stack[i+current];
			stack[i]=temp_stack[i];
		}
		for(int i=STACK_SIZE-current; i<STACK_SIZE; i++)
			stack[i]=null;

		//finally, overwrite current state
		current = 0;
	}
	
	public Object[] undoLoad()
	{
		current++;
		return XMLToObjs(stack[current]);
	}
	
	public Object[] redoLoad()
	{
		current--;
		return XMLToObjs(stack[current]);
	}
	
	//use to enable/disable redo functionality
	public boolean redoPossible()
	{
		return (current != 0);
	}
	
	//use to enable/disable undo functionality
	public boolean undoPossible()
	{
		return (stack[current+1] instanceof String);
	}
	
	private String ObjsToXML(Object[] obj)
	{
		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(BAOS);
		for(int i=0; i<obj.length; i++)
			encoder.writeObject(obj[i]);
		encoder.close();
		
		return BAOS.toString();
	}
	
	private Object[] XMLToObjs(String xml)
	{
		ByteArrayInputStream sr = new ByteArrayInputStream(xml.getBytes());
		XMLDecoder decoder = new XMLDecoder(sr);
		Object[] out = new Object[num_objs];
		for(int i=0; i<num_objs; i++)
			out[i] = decoder.readObject();
		decoder.close();
		
		return out;
	}
}