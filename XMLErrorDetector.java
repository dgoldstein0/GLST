import java.beans.ExceptionListener;


public class XMLErrorDetector implements ExceptionListener {

	private boolean is_error;
	
	public XMLErrorDetector()
	{
		is_error=false;
	}
	
	@Override
	public void exceptionThrown(Exception e) {
		e.printStackTrace();
		is_error=true;
	}
	
	public boolean isError()
	{
		return is_error;
	}
}
