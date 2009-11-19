import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SimpleSocketClient {

  public static void main(String args[]) throws Exception {
    Socket socket;
    int portNumber = 1777;
    String str;

    socket = new Socket(InetAddress.getLocalHost(), portNumber);
    
    //XMLDecoder decoder = new XMLDecoder(socket.getInputStream());
  //  str =(String) decoder.readObject();
    
    XMLEncoder encoder = new XMLEncoder(socket.getOutputStream());
    str="11";
    encoder.writeObject(str);
    
    encoder.close();
    
    socket.close();
    
  //  System.out.println(str);
  }

}