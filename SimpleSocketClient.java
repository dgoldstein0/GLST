import java.beans.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SimpleSocketClient
{
	public static void main(String args[]) throws Exception
	{
		Socket socket;
		int portNumber = 1777;
		//Galaxy str;
		String str;
		
		Galaxy map;
		//String map;
		
		File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
		try
		{
			XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(cur_file)));
			map = (Galaxy)d.readObject();
			d.close();
			
			if(map == null)
				System.out.println("where's the map?");
			
			//map="EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEECHOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO!";
			
			/*String ip_in_string;                                            
			ip_in_string=JOptionPane.showInputDialog("Enter IP address");
			byte[] ip_in_byte=new byte[4];
			String[] ip=ip_in_string.split("\\.");              
			for (int i=0; i<=3; i++)
				ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);      
			InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);*/
			InetAddress ipaddress = InetAddress.getLocalHost();
			
			TimeControl tc=new TimeControl(0);
			
			socket = new Socket(ipaddress, portNumber);		    
			OutputStream OS=socket.getOutputStream();
			InputStream IS = socket.getInputStream();
			
			XMLEncoder2 encoder = new XMLEncoder2(new BufferedOutputStream(OS));
			encoder.writeObject(map);
			encoder.finish();
			//OS.close();
			//socket.close();
			
			//OS = socket.getOutputStream();

			//socket = new Socket(ipaddress, portNumber);
		    
			/*XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(IS));
			str =(String) decoder.readObject();
			boolean x = str.equals(map);
			if(x)
				System.out.println("response is the same as original");
			else
				System.out.println("failure");
			System.out.println(Long.toString(tc.getTime()));
			*/
			Thread.sleep(10000);
			
			OS.close();
			encoder.close();
			//decoder.close();
			IS.close();
			socket.close();
		    
		  //  System.out.println(str);
				
		}
		catch(FileNotFoundException e)
		{
			System.err.println("File not found exception in function load");
		}
	}
	
	/*public static class waiter implements Runnable
	{
		int time;
		
		public waiter(int t)
		{
			time=t;
		}
		
		public void run()
		{
			sleep(time);
		}
	}*/
}