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
    String str="11";
    
    serverSocket = new ServerSocket(portNumber);

    System.out.println("Waiting for a connection on " + portNumber);

    socket = serverSocket.accept();

    
    XMLDecoder decoder = new XMLDecoder(socket.getInputStream());
     str =(String) decoder.readObject();
     System.out.println(str);
    
    //XMLEncoder encoder = new XMLEncoder(socket.getOutputStream());
   // encoder.writeObject(str);
  
        
    decoder.close();

    socket.close();

  }

}
