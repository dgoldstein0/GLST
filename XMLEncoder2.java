import java.beans.*;
import java.io.*;

public class XMLEncoder2 extends XMLEncoder
{
	OutputStream out;
	private static String encoding = "UTF-8";
	
	public XMLEncoder2(OutputStream out)
	{
		super(out);
		this.out=out;
	}
	
	public void finish()
	{
		flush();
		writeln("</java>");
		flush();
	}
	
	private void writeln(String  exp) {
         try {
             /*for(int i = 0; i < indentation; i++) {
                 out.write(' ');
             }*/
             out.write(exp.getBytes(encoding));
             out.write(" \n".getBytes(encoding));
         }
         catch (IOException e) {
             getExceptionListener().exceptionThrown(e);
         }
     }
}
