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
	Player the_player;
	HashSet<Player> players;
	private int last_id;
	Galaxy map;
	volatile boolean game_started;

	GameLobby GL;
	GameStartupDialog GSD;
	JFrame frame;
	
	volatile ServerSocket the_server_socket; 
	volatile Socket the_socket;
	volatile OutputStream OS;
	volatile InputStream IS;
	boolean hosting;//true if running as server  false if running as client
	Thread serverThread;
	Thread readThread;
	
	Set<Event> pending_execution = Collections.synchronizedSet(new HashSet());
	
	public GameControl(JFrame f)
	{
		frame = f;
		players = new HashSet();
		last_id=0;
		map=new Galaxy();
	}
	
	public GameControl(){}
		
	public void createThePlayer() throws CancelException
	{
		the_player = Player.createPlayer();
		the_player.setId(last_id+1);
		last_id++;
	}
	
	public Player getThe_player(){return the_player;}
	public void setThe_player(Player p){the_player=p;}
	public Galaxy getMap(){return map;}
	public void setMap(Galaxy g){map=g;}
	public HashSet<Player> getPlayers(){return players;}
	public void setPlayers(HashSet<Player> h){players=h;}
	
	public void startupDialog()
	{
		if(GSD instanceof GameStartupDialog)
			GSD.constructDialog();
		else
			GSD = new GameStartupDialog(frame, this);
	}
	
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
				
				File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
				try
				{
					loadMap(cur_file);
					sendMap();
				}
				catch(FileNotFoundException fnfe)
				{
					System.out.println("File not found.  Ending Connection...");
					endConnection();
					return;
				}
				
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
						/*Thread t = new Thread();
						t.start();
						t.sleep(10);
						t.join();*/
						Thread.sleep(10);
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

				downloadAndLoadMap(false);
				System.out.println("map loaded");
				
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
			//updateGame(); //BOOKMARK!
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
		try{
			createThePlayer();
		} catch(CancelException e){
			startupDialog();
			return;
		}
		hosting=true;
		serverThread = new Thread(new HostRunnable());
		serverThread.start();
		GL=new GameLobby(frame, this);
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
			System.out.println("Host runnable running...");
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
				GL.leaveGame(true);
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
						System.out.println("Waiting for connections...");
						the_socket = the_server_socket.accept();
						go=false;
						setUpIOStreams();
						
						//send player roster.  start with the number of players, and then send the_player and then go through players hashset
						PrintWriter w = new PrintWriter(OS, true); //this should auto-flush
						BufferedReader r = new BufferedReader(new InputStreamReader(IS));
						
						//r.readLine(); //wait until client is ready
						int num_players = players.size()+1;
						w.println(Integer.toString(num_players));
						
						w.println(the_player.getName());
						w.println(Integer.toString(the_player.getId()));
						for(Player p : players){
							w.println(p.getName());
							w.println(Integer.toString(p.getId()));
						}
						
						
						//request name
						String name;
						name=r.readLine();
						Player p = new Player(name);
						p.setId(last_id+1); //assign ID
						players.add(p);
						
						//assign id number.  each ID is 1 more than last assigned
						
						w.println(Integer.toString(last_id+1));
						last_id++;
						
						//notify other players and update the Lobby //BOOKMARK
					}
					catch (SocketTimeoutException ste){}
					catch (IOException e)
					{
						System.out.println("Accept failed.");
						//keep trying?
					}
				}
			}
			catch(SocketException se)
			{
				System.out.println("hosting failed");
				GL.leaveGame(true);
				return;
			}
		}
	}
	
	public void joinAsClient() //throws UnknownHostException
	{
		try{
			createThePlayer();
		} catch(CancelException e){
			startupDialog();
			return;
		}
		
		System.out.println("joinAsClient started");
		
		try
		{
			if(the_socket instanceof Socket)
				the_socket.close();
		}
		catch(IOException e){}
		
		byte[] ip_in_byte = new byte[4];
		boolean ip_valid=false;
		while(!ip_valid) {
			try {
				String ip_in_string=JOptionPane.showInputDialog("Enter the IP address of the host:");
				if(ip_in_string instanceof String){
					String[] ip=ip_in_string.split("\\.");
					
					if(ip.length != 4)
						throw new NumberFormatException();
					
					for (int i=0; i<=3; i++){
						//System.out.println(ip[i]);
						ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);
					}
					System.out.println("IP address read");
					ip_valid=true;
				} else {
					startupDialog();
					return;
				}
			} catch(NumberFormatException nfe) {
				JOptionPane.showMessageDialog(frame, "This is not a valid IP address.  Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		try
		{	
			InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
			//InetAddress ipaddress=InetAddress.getLocalHost();
			the_socket = new Socket(ipaddress, DEFAULT_PORT_NUMBER);
			setUpIOStreams();
			hosting=false;
			
			//receive other players' names and id's
			BufferedReader r = new BufferedReader(new InputStreamReader(IS));
			PrintWriter pw = new PrintWriter(OS, true); //this version of the constructor sets up automatic flushing
			
			int num_players = Integer.parseInt(r.readLine());
			for(int i=0; i<num_players; i++){
				Player p = new Player(r.readLine());
				p.setId(Integer.parseInt(r.readLine()));
				players.add(p);
			}
			
			
			//send name
			pw.println(the_player.getName());
			
			//recieve player id number			
			the_player.setId(Integer.parseInt(r.readLine()));
			
			System.out.println("Connection Established");
			
			//display lobby
			GL = new GameLobby(frame, this);
		}
		catch (UnknownHostException e)
		{
			System.err.println("Unknown host");
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to the host");
			JOptionPane.showMessageDialog(frame, "The specified IP could not be reached.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
		}
		catch(NumberFormatException e)
		{
			System.err.println("Connection lost");
			JOptionPane.showMessageDialog(frame, "Connection Lost.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
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
		try{OS.close();}catch(Exception e){}
		try{IS.close();}catch(Exception e){}
		try{the_socket.close();}catch(Exception e){}
	}
}