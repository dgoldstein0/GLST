import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.ObjectOutputStream;
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

    
     XMLDecoder decoder = new XMLDecoder(socket.getInputStream());
     Galaxy str =(Galaxy) decoder.readObject();
     System.out.println("error");
   //  System.out.println(str);     
     decoder.close();
     socket.close();
     //str="1afdgfshbsafeqwtfwrgwahfdhdfwegwrhwhfbfeagWTWGAHFDHAHRWHEAHBGDNBWAHWHWHHBFDSGSWQETQSFGSGFDHHFHWAGRGWAGFSAGRWGRGFGDSGR1";
     socket = serverSocket.accept();
     
     XMLEncoder encoder=new XMLEncoder(socket.getOutputStream());
     encoder.writeObject(str);
    
    //XMLEncoder encoder = new XMLEncoder(socket.getOutputStream());
   // encoder.writeObject(str);  
    encoder.close();  
    socket.close();

  }

}
