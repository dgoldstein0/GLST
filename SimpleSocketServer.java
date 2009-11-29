import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleSocketServer {

  public static void main(String args[]) throws Exception {
    ServerSocket serverSocket;
    int portNumber = 1777;
    Socket socket;
   // String str="1afdgfshbsafeqwtfwrgwahfdhdfwegwrhwhfbfeagWTWGAHFDHAHRWHEAHBGDNBWAHWHWHHBFDSGSWQETQSFGSGFDHHFHWAGRGWAGFSAGRWGRGFGDSGR1";
    
    serverSocket = new ServerSocket(portNumber);

    System.out.println("Waiting for a connection on " + portNumber);

    socket = serverSocket.accept();
	InputStream IS = socket.getInputStream();
 	OutputStream OS = socket.getOutputStream(); 
	
	BufferedReader reader = new BufferedReader(new InputStreamReader(IS));

	String line=reader.readLine();
	StringBuffer str = new StringBuffer("");
	Boolean kill=false;
	while(line != null && !kill)
	{
		str.append(line);
		System.out.println(line);
		if(line.indexOf("</java>") == -1)
			line = reader.readLine();
		else
			kill=true;
	}
	
	//for(int i=0; i<50; i++)
	//	System.out.println(Integer.toString(str.codePointAt(str.length()-50+i)));
	
	FileWriter FW = new FileWriter("C:\\Users\\David\\Desktop\\network_test.xml");
	FW.write(str.toString(),0,str.length());
	FW.close();
	
	ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
	XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
	System.out.println("Checkpoint");
	Galaxy result =(Galaxy) decoder.readObject();
	System.out.println("Checkpoint 2");
	decoder.close();

     //socket.close();
     //str="1afdgfshbsafeqwtfwrgwahfdhdfwegwrhwhfbfeagWTWGAHFDHAHRWHEAHBGDNBWAHWHWHHBFDSGSWQETQSFGSGFDHHFHWAGRGWAGFSAGRWGRGFGDSGR1";
    // socket = serverSocket.accept();
     
     //XMLEncoder encoder=new XMLEncoder(new BufferedOutputStream(OS));
     //encoder.writeObject(str);
    
    decoder.close();
    reader.close();
    //encoder.close();  
    socket.close();

  }

}
