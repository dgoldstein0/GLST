import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.*;

public class SimpleSocketClient {

  public static void main(String args[]) throws Exception {
    Socket socket;
    int portNumber = 1777;
    Galaxy str;

	Galaxy map;
	File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
	try
	{
		XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(cur_file)));
		map = (Galaxy)d.readObject();
		d.close();
		
		if(!(map instanceof Galaxy))
			System.out.println("where's the map?");
		
		String ip_in_string;                                            
		ip_in_string=JOptionPane.showInputDialog("Enter IP address");
		byte[] ip_in_byte=new byte[4];
		String[] ip=ip_in_string.split("\\.");              
		for (int i=0; i<=3; i++)
			ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);      
		InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
		
		TimeControl tc=new TimeControl();
		
		socket = new Socket(ipaddress, portNumber);
	    
	    
		OutputStream os=socket.getOutputStream();
		XMLEncoder encoder = new XMLEncoder(os);
		encoder.writeObject(map);
		encoder.close();
		//os.close();
		socket.close();

		socket = new Socket(ipaddress, portNumber);
	    
		XMLDecoder decoder = new XMLDecoder(socket.getInputStream());
		str =(Galaxy) decoder.readObject();
		boolean x = str.equals(map);
		if(x)
			System.out.println("response is the same as original");
		else
			System.out.println("failure");
		System.out.println(Long.toString(tc.getTime()));
		
		decoder.close();
		socket.close();
	    
	  //  System.out.println(str);
			
	}
	catch(FileNotFoundException e)
	{
		System.err.println("File not found exception in function load");
	}
	  

  }

}