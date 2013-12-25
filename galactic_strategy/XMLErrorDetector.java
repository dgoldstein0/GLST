package galactic_strategy;
import java.beans.ExceptionListener;

import org.xml.sax.SAXParseException;


public class XMLErrorDetector implements ExceptionListener {

	private boolean is_error;
	
	public XMLErrorDetector()
	{
		is_error=false;
	}
	
	@Override
	public void exceptionThrown(Exception e) {
		
		if (e instanceof SAXParseException){
			SAXParseException spe = (SAXParseException) e;
			System.err.println(spe.toString());
			System.err.println("Line number: " + spe.getLineNumber());
			System.err.println("Column number: " + spe.getColumnNumber() );
			System.err.println("Public ID: " + spe.getPublicId() );
			System.err.println("System ID: " + spe.getSystemId());
		}
		
		e.printStackTrace();
		is_error=true;
	}
	
	public boolean isError()
	{
		return is_error;
	}
}
