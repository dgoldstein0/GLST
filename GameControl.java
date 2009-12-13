import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

public class GameControl
{
	static final int DEFAULT_PORT_NUMBER = 7007;
	
	TimeControl TC;
	Player player;
	Galaxy map;
	volatile ServerSocket the_server_socket; 
	volatile Socket the_socket;
	volatile OutputStream OS;
	volatile InputStream IS;
	boolean hosting;//true if running as server  false if running as client
	Thread serverThread;
	Thread readThread;
	
	Set<Event> pending_execution = Collections.synchronizedSet(new HashSet());
	
	public GameControl(JFrame frame)
	{
		player=Player.createPlayer();
		map=new Galaxy();
	}
	
	public GameControl(){}
	
	public Player getPlayer(){return player;}
	public void setPlayer(Player p){player=p;}
	public Galaxy getMap(){return map;}
	public void setMap(Galaxy g){map=g;}
	
	public void startGame()
	{
		//stop the server socket.  Once the game is started, more players cannot join.
		try{serverThread.join();}
		catch(InterruptedException IE)
		{
			System.out.println("what the heck? The main thread has been interrupted");
			return;
		}
		
		try
		{
			if(the_server_socket instanceof ServerSocket)
				the_server_socket.close();
		}
		catch(IOException e){}

		readThread = new Thread(new EventReader());
		//Start time
		TC = new TimeControl();
	}
	
	public void updateGame()
	{
		long time_elapsed=TC.getTime();
		
		//start events that need to occur before time_elapsed
		
		for(Event e: pending_execution)
		{
			if(e.scheduled_time >= time_elapsed)
			{
				e.run();
				pending_execution.remove(e);
			}
		}
		
		//update all data
		for(GSystem sys : map.systems)
		{
			for(Satellite sat : sys.orbiting_objects)
			{
				if(sat instanceof Planet)
				{
					for(Facility f : ((Planet)sat).facilities)
					{
						f.newTime(time_elapsed);
					}
					for(Satellite sat2 : ((Planet)sat).satellites)
					{
						if(sat2 instanceof Moon)
						{
							for(Facility f : ((Moon)sat2).facilities)
								f.newTime(time_elapsed);
						}
					}
				}
			}
		}
		
		//draw everything
	}
	
	public void host() //creates new thread to host the game on
	{
		serverThread = new Thread(new HostRunnable());
		serverThread.start();
	}
	
	public void endHost()
	{
		serverThread.interrupt();
	}
	
	public class HostRunnable implements Runnable
	{
		public void HostRunnable()
		{
		}
		
		public void run()
		{
			try
			{
				if(the_server_socket instanceof ServerSocket)
					the_server_socket.close();
			}
			catch(IOException e){}
			
			try{the_server_socket = new ServerSocket(DEFAULT_PORT_NUMBER);}
			catch (IOException e)
			{
				System.out.println("Could not listen on port" + Integer.toString(DEFAULT_PORT_NUMBER)+".  Hosting failed.");
				return;
			}
			
			try
			{
				if(the_socket instanceof Socket)
					the_socket.close();
			}
			catch(IOException e){}
			
			try
			{
				the_server_socket.setSoTimeout(500);
				boolean go=true;
				while(!Thread.interrupted() && go)
				{
					try
					{
						the_socket = the_server_socket.accept();
						go=false;
						setUpIOStreams();
					}
					catch (SocketTimeoutException ste){}
					catch (IOException e) {System.out.println("Accept failed.");}
				}
			}
			catch(SocketException se){System.out.println("hosting failed");}
		}
	}
	
	public void joinAsClient() //throws UnknownHostException
	{
		try
		{
			if(the_socket instanceof Socket)
				the_socket.close();
		}
		catch(IOException e){}
		
		/*String ip_in_string;                                            
		ip_in_string=JOptionPane.showInputDialog("Enter the IP address of the host:");
		byte[] ip_in_byte=new byte[4];
		String[] ip=ip_in_string.split(".");              
		for (int i=0; i<=3; i++)
			ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);      */

        
		try {	
			//InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
			InetAddress ipaddress=InetAddress.getLocalHost();
			the_socket = new Socket(ipaddress, DEFAULT_PORT_NUMBER);   
			setUpIOStreams();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host");
		}
	}
	
	private void setUpIOStreams() throws IOException
	{
		IS = the_socket.getInputStream();
		OS = the_socket.getOutputStream();
	}
	
	public void notifyAllPlayers(Event e)
	{
		XMLEncoder2 encoder = new XMLEncoder2(OS);
		encoder.writeObject(e);
		encoder.finish();
	}
	
	public void downloadAndLoadMap(boolean SAVE) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(IS));

		String line=reader.readLine();
		StringBuffer str = new StringBuffer("");
		Boolean kill=false;
		while(line != null && !kill)
		{
			str.append(line);
			if(line.indexOf("</java>") == -1)
				line = reader.readLine();
			else
				kill=true;
		}
		
		ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
		Galaxy map =(Galaxy) decoder.readObject();
		decoder.close();
		
		if(SAVE)
		{
			FileWriter FW = new FileWriter("C:\\Users\\David\\Desktop\\network_test.xml");
			FW.write(str.toString(),0,str.length());
			FW.close();
		}
	}
	
	public void loadMap(File f) throws FileNotFoundException
	{
		XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
		map = (Galaxy)d.readObject();
		d.close();
	}
	
	public void sendMap()
	{
		XMLEncoder2 e = new XMLEncoder2(OS);
		e.writeObject(map);
		e.finish();
	}
	
	public class EventReader implements Runnable
	{
		public EventReader(){}
			
		public void run()
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(IS));
			try
			{
				while(!Thread.interrupted())
				{
					String line="";
					StringBuffer str = new StringBuffer("");
					Boolean kill=false;
					while(!kill)
					{
						str.append(line);
						if(line.indexOf("</java>") == -1)
							line = br.readLine();
						else
							kill=true;
					}
					
					ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
					XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
					Event e =(Event) decoder.readObject();
					decoder.close();
					pending_execution.add(e);
				}
			}
			catch(IOException ioe){System.err.println("Connection lost.");}//terminate.  exception due to either readLine or due to unsupported encoding (UTF-8)
		}
	}
	
	public void endConnection()
	{
		try{OS.close();}catch(IOException e){}
		try{IS.close();}catch(IOException e){}
		try{the_socket.close();}catch(IOException e){}
	}
}