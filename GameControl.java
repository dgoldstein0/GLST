import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

public class GameControl
{
	static final int DEFAULT_PORT_NUMBER = GalacticStrategyConstants.DEFAULT_PORT_NUMBER;
	
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
		System.out.println("starting game...");
		//stop the server socket.  Once the game is started, more players cannot join.
		/*try{serverThread.join();}
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
		catch(IOException e){}*/

		try
		{
			PrintWriter writer = new PrintWriter(OS, true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(IS));
			
			if(hosting)
			{
				System.out.println("host start sequence begin");
				
				//estimate time delay
				
				//ping
				boolean pong=false;
				long[] pingtimes = new long[100];
				long pongtime=0;
				String pongmsg="";
				for(int i=0; !pong && i<100; i++)
				{
					writer.println("ping" + Integer.toString(i));
					pingtimes[i] = System.nanoTime();
					System.out.println("ping" + Integer.toString(i));
					//pong
					if(reader.ready())
					{
						pongmsg = reader.readLine();
						pongtime=System.nanoTime();
						pong=true;
						System.out.println("that was a pong!");
					}
					try
					{
						Thread t = new Thread();
						t.start();
						t.sleep(10);
						t.join();
					}
					catch(InterruptedException ie){}
				}
				
				if(!pong) //if pongtime is 0 or (and) pongmsg empty - i.e. no pong recieved - then the function will die here, and not try to process a nonexistent pong
				{
					System.out.println("Connection Lost.  Closing connection.");
					endConnection();
					return;
				}
				
				//figure out which ping was returned, and compute estimate
				
				int offset_estimate = (int)((pongtime-pingtimes[Integer.parseInt(pongmsg)])/2);
				System.out.println("Offset estimated: "+Integer.toString(offset_estimate));
				
				//send offset_estimate
				writer.println(Integer.toString(offset_estimate));
				System.out.println("estimate sent");
				
				//Start time
				TC=new TimeControl(0);
				System.out.println("time started!");
				
				//send start signal
				
				writer.println("Start");
				System.out.println("Start signal sent!");
			}
			else
			{
				System.out.println("Client start sequence initiated.");

				//return message for offset estimate
				System.out.println("waiting for ping");
				String pingmsg= reader.readLine();
				System.out.println("pong time");
				writer.println(pingmsg.substring(4));
				System.out.println("pong!");
				
				//recieve offset_estimate
				String received;
				do
				{
					received=reader.readLine();
					System.out.println(received);
				}
				while(received.indexOf("ping") != -1);
				
				int offset = Integer.parseInt(received);
				System.out.println("offset recieved");
				
				//start time when start signal is recieved
				reader.readLine();
				TC = new TimeControl(offset);
			}
		}
		catch(IOException ioe)
		{
			System.out.println("Start Game failed.  Ending Connection...");
			endConnection();
			return;
		}
		
		//start event reading
		readThread = new Thread(new EventReader());
		readThread.start();
		
		//set game to update itself
		TC.startConstIntervalTask(new Updater(),5);
	}
	
	private class Updater extends TimerTask
	{
		private Updater(){}
		
		public void run()
		{
			//updateGame();
			System.out.println("update!!" + Long.toString(TC.getTime()));
		}
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
		hosting=true;
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
		System.out.println("joinAsClient started");
		
		try
		{
			if(the_socket instanceof Socket)
				the_socket.close();
		}
		catch(IOException e){}
		
		//String ip_in_string;                                            
		String ip_in_string=JOptionPane.showInputDialog("Enter the IP address of the host:");
		byte[] ip_in_byte=new byte[4];
		String[] ip=ip_in_string.split("\\.");              
		for (int i=0; i<=3; i++)
		{
			//System.out.println(ip[i]);
			ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);
		}
		System.out.println("IP address read");
        
		try {	
			InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
			//InetAddress ipaddress=InetAddress.getLocalHost();
			the_socket = new Socket(ipaddress, DEFAULT_PORT_NUMBER);
			setUpIOStreams();
			hosting=false;
			System.out.println("Connection Established");
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
	
	public void downloadAndLoadMap(boolean SAVE) throws IOException //for the client
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
	
	public void loadMap(File f) throws FileNotFoundException //for the server
	{
		XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
		map = (Galaxy)d.readObject();
		d.close();
	}
	
	public void sendMap() //for the server
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
					System.out.println("reading object...");
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
			catch(IOException ioe)
			{
				System.err.println("Connection lost.");
				endConnection();
				return;
			}//terminate.  exception due to either readLine or due to unsupported encoding (UTF-8)
		}
	}
	
	public void endConnection()
	{
		try{OS.close();}catch(IOException e){}
		try{IS.close();}catch(IOException e){}
		try{the_socket.close();}catch(IOException e){}
	}
}