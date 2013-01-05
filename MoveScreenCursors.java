//this class sets up custom cursors used to scroll around the system
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Point;

public class MoveScreenCursors
{
	Cursor left_arrow;
	Cursor right_arrow;
	Cursor up_arrow;
	Cursor down_arrow;
	Cursor up_right_arrow;
	Cursor up_left_arrow;
	Cursor down_right_arrow;
	Cursor down_left_arrow;
	
	public MoveScreenCursors()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/right_arrow.png"), new Point(30,15), "right arrow");
		left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/left_arrow.png"), new Point(0,15), "left arrow");
		up_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_arrow.png"), new Point(15,0), "up arrow");
		down_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_arrow.png"), new Point(15,30), "down arrow");
		
		up_right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_right_arrow.png"), new Point(30,0), "up right arrow");
		up_left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/up_left_arrow.png"), new Point(0,0), "up left arrow");
		down_right_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_right_arrow.png"), new Point(30,30), "down right arrow");
		down_left_arrow = toolkit.createCustomCursor(toolkit.getImage("images/down_left_arrow.png"), new Point(0,30), "down left arrow");
	}
	
	public Cursor left(){return left_arrow;}
	public Cursor right(){return right_arrow;}
	public Cursor up(){return up_arrow;}
	public Cursor down(){return down_arrow;}
	public Cursor upRight(){return up_right_arrow;}
	public Cursor upLeft(){return up_left_arrow;}
	public Cursor downRight(){return down_right_arrow;}
	public Cursor downLeft(){return down_left_arrow;}
}