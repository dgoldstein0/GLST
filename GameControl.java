import javax.swing.JOptionPane;

import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

import javax.swing.SwingUtilities;

import javax.swing.JFileChooser;
import java.awt.Color;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.concurrent.PriorityBlockingQueue;

public class GameControl
{
	static final int DEFAULT_PORT_NUMBER = GalacticStrategyConstants.DEFAULT_PORT_NUMBER;
	static final String LEFT_LOBBY_MSG = "Im leaving the lobby.";
	static final String MAP_CHOSEN = "Host chooses map::"; //do not change ending
	static final String READY_MSG = "I'm ready, let's start!";
	static final String NOT_READY_MSG = "wait, I take that back, I'm not ready";
	static final String START_MSG = "START THE GAME";
	
	TimeControl TC;
	int player_id;
	Player[] players;
	Galaxy map;

	JFileChooser filechooser; //for single player testing
	
	GameInterface GI;
	GameLobby GL;
	GameStartupDialog GSD;
	
	volatile ServerSocket the_server_socket; 
	volatile Socket the_socket;
	volatile OutputStream OS;
	volatile InputStream IS;
	boolean hosting;//true if running as server  false if running as client
	Thread serverThread; //waits for connections
	Thread lobbyThread; //reads data and updates the GameLobby
	Thread readThread; //reads data during the game
	Thread startThread; //processes start game.   This is a separate Thread because it can crash the interface if run on swing's event thread
	
	PriorityBlockingQueue<Order> pending_execution;
	
	public GameControl(GameInterface gi)
	{
		GI = gi;
		try {
			GalacticStrategyConstants.ImageLoader(); //preload images
		} catch (IOException e) {
			System.out.println("trouble reading images");
			e.printStackTrace();
		}
		
		players = new Player[GalacticStrategyConstants.MAX_PLAYERS];
		map=new Galaxy();
		GameInterface.GC = this;
		
		//preload file chooser for singlePloyerTest map loading
		filechooser = new JFileChooser();
		filechooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));
		pending_execution = new PriorityBlockingQueue<Order>();
	}
	
	public GameControl()
	{
		try {
			GalacticStrategyConstants.ImageLoader(); //preload images
		} catch (IOException e) {
			System.out.println("trouble reading images");
			e.printStackTrace();
		}
		
		GameInterface.GC = this;
	}
		
	public Player createThePlayer() throws CancelException
	{
		return Player.createPlayer();
	}
	
	public int nextAvailableID()
	{
		for(int i=0; i<players.length; i++){
			if(!(players[i] instanceof Player))
			{
				System.out.println(Integer.toString(i) + "is available!");
				return i;
			}
		}
		System.out.println("no id's available!");
		return -1; //indicates error
	}
	
	public int getNumberOfPlayers()
	{
		int num_players=0;
		for(int i=0; i<players.length; i++){
			if(players[i] instanceof Player)
				num_players++;
		}
		return num_players;
	}
	
	public int getPlayer_id(){return player_id;}
	public void setPlayer_id(int id){player_id=id;}
	public Galaxy getMap(){return map;}
	public void setMap(Galaxy g){map=g;}
	public Player[] getPlayers(){return players;}
	public void setPlayers(Player[] h){players=h;}
	
	public void startupDialog()
	{
		if(GSD instanceof GameStartupDialog)
			GSD.constructDialog();
		else
			GSD = new GameStartupDialog(GI.frame, this);
	}
	
	private void startGame()
	{
		System.out.println("starting game...");
		
		try{
			lobbyThread.interrupt();
			lobbyThread.join();
			System.out.println("lobby thread terminated");
		} catch(InterruptedException e){
			System.out.println("Start game has been interrupted.  Terminating...");
			endConnection();
			return;
		}
		
		//BOOKMARK - This next block is commented out since it is not yet necessary.  Once one player joins
		//the server socket closes; if the game is extended to include more than just the 2 player mode, this
		//will be necessary.
		
		//stop the server socket, so that once the game is started, more players cannot join.
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
				
				writer.println(START_MSG);
				
				//File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
				boolean retry=true;
				while(retry)
				{
					if(!Thread.interrupted()){
						if(reader.ready()){
							reader.readLine();//wait for client
							sendMap();
							retry=false;
						} else {
							Thread.sleep(20);
						}
					} else {
						System.out.println("Aborting Start sequence");
					}
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
					if(reader.ready()) {
						pongmsg = reader.readLine();
						pongtime=System.nanoTime();
						pong=true;
						System.out.println("that was a pong!");
					}
					try {
						Thread.sleep(10);
					}
					catch(InterruptedException ie){}
				}
				
				if(!pong) //if pongtime is 0 or (and) pongmsg empty - i.e. no pong recieved - then the function will die here, and not try to process a nonexistent pong
				{
					System.out.println("Connection Lost.  Closing connection.");
					GL.leaveGame(false);
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

				writer.println("I'm ready!");//helps make sure the map is sent safely
				downloadAndLoadMap(false);
				System.out.println("map loaded");
				
				//return message for offset estimate
				System.out.println("waiting for ping");
				String pingmsg= reader.readLine(); //impractical to make this non-blocking, since it will mess up timing measure
				System.out.println("pong time");
				writer.println(pingmsg.substring(4));
				System.out.println("pong!");
				
				//recieve offset_estimate
				String received="";
				do
				{
					boolean new_line_read=false;
					while(!new_line_read){ //readLine in a nonblocking manner, ending startGame on interrupts
						if(!Thread.interrupted()){
							if(reader.ready()){
								received=reader.readLine();
								System.out.println(received);
								new_line_read=true;
							} else {
								Thread.sleep(20);
							}
						} else {
							System.out.println("Interruption!  Ending start game...");
							return;
						}
					}
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
		catch(InterruptedException ie) //interrupt generated by GL.leaveGame
		{
			System.out.println("Start game interrupted.  Leaving game...");
			return;
		}
		
		//start event reading
		readThread = new Thread(new EventReader());
		readThread.start();
		
		//set up systems for the game
		for(GSystem sys : map.systems)
			sys.setUpForGame(this);
		
		//start everyone in assigned locations
		for(int i=0; i<map.start_locations.size(); i++)
		{
			OwnableSatellite<?> p = map.start_locations.get(i);
			p.setOwner(players[i]);
			Base b = new Base(p, p.next_facility_id++, (long)0);
			p.facilities.put(b.id,b);
			p.the_base = b;
		}
		
		map.saveOwnablesData();
		
		//start game graphics...
		GI.drawGalaxy();
		
		//set game to update itself
		TC.startConstIntervalTask(new Updater(),(int)GalacticStrategyConstants.TIME_GRANULARITY);
	}
	
	public void startGameViaThread()
	{
		if(GL != null)
			GL.dispose();
			
		startThread = new Thread(new Runnable(){public void run(){startGame();}});
		startThread.start();
	}
	
	public void startSinglePlayerTest()
	{
		try{
			Player the_player = createThePlayer();
			player_id=0;
			the_player.setId(0);
			the_player.setColor(Color.GREEN);
			players[0] = the_player;
		} catch(CancelException e){
			startupDialog();
			return;
		}
		
		//CHOOSE AND LOAD MAP
		
		//stolen from GameLobby.actionPreformed
		int val = filechooser.showOpenDialog(GI.frame);
		if(val==JFileChooser.APPROVE_OPTION){
			File map_file = filechooser.getSelectedFile();
			
			//load the map.  notify if errors.  This is supposed to validate the map by attempting to load it
			
			try{
				loadMap(map_file); //parsing errors render the map invalid, causing one of the messages in the catch statements.
				//the existence of the name is the second line of defense.
				if(!(map.getName() instanceof String)){
					map=null;
					JOptionPane.showMessageDialog(GI.frame, "The selected file is not a completed map.  Please pick a different map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch(FileNotFoundException fnfe) {
				JOptionPane.showMessageDialog(GI.frame, "The file was not found.  Please choose another file.", "Error - File not Found", JOptionPane.ERROR_MESSAGE);
				return;
			} catch(ClassCastException cce) {
				JOptionPane.showMessageDialog(GI.frame, "The file you have selected is not a map", "Class Casting Error", JOptionPane.ERROR_MESSAGE);
				return;
			} catch(NullPointerException npe) {
				JOptionPane.showMessageDialog(GI.frame, "Map loading failed.  The selected file is not a valid map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			TC = new TimeControl(0);
			
			//set up systems for the game
			for(GSystem sys : map.systems)
				sys.setUpForGame(this);
			
			//start the player in assigned location
			OwnableSatellite<?> p = map.start_locations.get(player_id);
			p.setOwner(players[player_id]);
			Base b = new Base(p, p.next_facility_id++, (long)0);
			p.facilities.put(b.id,b);
			p.the_base = b;
			
			map.saveOwnablesData();
			
			//display the Galaxy
			GI.drawGalaxy();
			
			//set game to update itself
			TC.startConstIntervalTask(new Updater(),20);
		} else {
			startupDialog();
		}
	}
	
	public void host() //creates new thread to host the game on
	{
		try{
			Player the_player = createThePlayer();
			player_id=0;
			the_player.setId(0);
			players[0] = the_player;
		} catch(CancelException e){
			startupDialog();
			return;
		}
		hosting=true;
		GL=new GameLobby(GI.frame, this);
		serverThread = new Thread(new HostRunnable());
		serverThread.start();
	}
	
	public void endHost() //closes down the serverThread, which is responsible for listening for players trying to join
	{
		serverThread.interrupt();
	}
	
	public class HostRunnable implements Runnable
	{
		public HostRunnable()
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
				GL.leaveGame(false);
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
						int num_players = players.length;
						w.println(Integer.toString(num_players));
						
						for(int i=0; i<num_players; i++){
							if(players[i] instanceof Player){
								w.println(players[i].getName());
								w.println(Integer.toString(i));
								w.println(Boolean.toString(players[i].ready));
							} else {
								w.println("skip player>>");
							}
						}
						
						
						//request name
						String name;
						name=r.readLine();
						Player p = new Player(name);
						
						//assign id number.  each ID is 1 more than last assigned
						int next_id = nextAvailableID();
						p.setId(next_id); //assign ID
						players[next_id] = p;
						w.println(Integer.toString(next_id));
						
						//notify other players and update the Lobby
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								updateGL();
							}
						});
						
						setUpLobbyUpdater(); //THIS WILL BE AN ISSUE FOR 3+ PLAYER GAMES.  ESPECIALLY IF ONLY 1 LobbyUpdater used, which checks for msgs from all.  Then we cannot let LobbyUpdater check for updates before player is done being set up
						mapChosen(); //notify new player of currently chosen map - or lack thereof						
					}
					catch (SocketTimeoutException ste){}
					catch (IOException e)
					{
						System.out.println("Accept failed.");
						//keep trying
					}
				}
			}
			catch(SocketException se)
			{
				System.out.println("hosting failed");
				GL.leaveGame(false);
				return;
			}
		}
	}
	
	public void joinAsClient() //runs on event thread
	{
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
				JOptionPane.showMessageDialog(GI.frame, "This is not a valid IP address.  Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		Player the_player;
		try{
			the_player = createThePlayer();
		} catch(CancelException e){
			startupDialog();
			return;
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
				String name_input = r.readLine();
				if(!name_input.equals("skip player>>")) {
					Player p = new Player(name_input);
					p.setId(Integer.parseInt(r.readLine()));
					p.setReady(Boolean.parseBoolean(r.readLine()));
					players[p.getId()] = p;
				}
			}
			
			
			//send name
			pw.println(the_player.getName());
			
			//recieve player id number
			player_id = Integer.parseInt(r.readLine());
			the_player.setId(player_id);
			players[player_id] = the_player;
			
			System.out.println("Connection Established");
			
			//display lobby.  This order is important.  The lobby must be created before we create the updater for it, since the Host immediately fires off a message containing the current choice of map, but framed as an update
			GL = new GameLobby(GI.frame, this);
			setUpLobbyUpdater();
		}
		catch (UnknownHostException e)
		{
			System.err.println("Unknown host");
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to the host");
			JOptionPane.showMessageDialog(GI.frame, "The specified IP could not be reached.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
		}
		catch(NumberFormatException e)
		{
			System.err.println("Connection lost");
			JOptionPane.showMessageDialog(GI.frame, "Connection Lost.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
		}
	}
	
	private void updateGL()
	{
		GL.updateNames();
		GL.start_game.setEnabled(GL.readyToStart()); //check to see if the player was the only thing holding up the game - or allowing it to start.  If so, enable/disable start button appropriately
	}
	
	private void setUpLobbyUpdater()
	{
		lobbyThread = new Thread(new LobbyUpdater());
		lobbyThread.start();
	}
	
	//Right now, this is set up based on the 2 player/1 connection model.  The lobbyUpdater, in it's current form, only listens to see if the single other player drops
	
	public class LobbyUpdater implements Runnable
	{
		public LobbyUpdater(){}
		
		public void run()
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(IS));
			try {
				while(!Thread.interrupted()){
					if(r.ready()) {
						String notification = r.readLine();
						String[] split_notification = notification.split(":");
						if(notification.indexOf(":")!= -1 && split_notification[1].equals(LEFT_LOBBY_MSG)) {
							if(hosting){
								int id_leaving = Integer.parseInt(split_notification[0]);
								players[id_leaving]=null;
								updateGL();
								serverThread = new Thread(new HostRunnable());
								serverThread.start();
								return;
							} else {
								JOptionPane.showMessageDialog(GI.frame, "The host has left the game.", "Host Left", JOptionPane.INFORMATION_MESSAGE);
								GL.leaveGame(false);
								return;
							}
						} else if(notification.indexOf(":")!= -1 && split_notification[1].equals(READY_MSG)) {
							//only the host should recieve this message
							int id_ready = Integer.parseInt(split_notification[0]);
							players[id_ready].ready=true;
							updateGL(); //this function takes care of enabling/disabling start button for us
						} else if(notification.indexOf(":")!= -1 && split_notification[1].equals(NOT_READY_MSG)) {
							//only the host should recieve this message
							int id_ready = Integer.parseInt(split_notification[0]);
							players[id_ready].ready=false;
							updateGL(); //this function takes care of enabling/disabling start button for us
						}
						else if(notification.indexOf(MAP_CHOSEN) != -1)
						{
							GL.map_label.setText(notification.split("::")[1]);
							GL.start_game.setEnabled(GL.readyToStart()); //check to see if we are ready to start the game, and enable start button if so.
						}
						else if(notification.equals(START_MSG))
						{
							startGameViaThread(); //must start on a different thread because start game terminates this one.  If start game ran on this thread, it would end itself.
						}
					}
					else
						Thread.sleep(200);
				}
			} catch(IOException ioe){
				System.out.println("IO fail in lobby updater.  GG.");
				return;
			} catch(InterruptedException ie){
				return;
			}
		}
	}
	
	public void leavingLobby() //responsible for informing other players of the user leaving the game.
	{
		if(OS instanceof OutputStream)
		{
			PrintWriter w = new PrintWriter(OS, true);
			w.println(Integer.toString(player_id) +":" + LEFT_LOBBY_MSG);
			players = new Player[GalacticStrategyConstants.MAX_PLAYERS];
		}
	}
	
	public void declareReady() //BOOKMARK - in a 3+ player game, this function will need to be modified to notify ALL players in the game
	{
		if(OS instanceof OutputStream)
		{
			PrintWriter w = new PrintWriter(OS, true);
			if(!players[player_id].ready)
			{
				w.println(Integer.toString(player_id) +":" + READY_MSG);
				players[player_id].ready=true;
			}
			else
			{
				w.println(Integer.toString(player_id) +":" + NOT_READY_MSG);
				players[player_id].ready=false;
			}
		}
	}
	
	public void mapChosen() //responsible for informing other players of map choice, or lack thereof
	{
		if(OS instanceof OutputStream)
		{
			PrintWriter w = new PrintWriter(OS, true);
			w.println(MAP_CHOSEN + GL.map_label.getText());
		}
	}
	
	private void setUpIOStreams() throws IOException
	{
		IS = the_socket.getInputStream();
		OS = the_socket.getOutputStream();
	}
	
	public void scheduleOrder(Order o)
	{
		pending_execution.offer(o);
		
		notifyAllPlayers(o);
	}
	
	public void notifyAllPlayers(Order o)
	{
		//notify other players
		if(OS != null)
		{
			XMLEncoder2 encoder = new XMLEncoder2(OS);
			encoder.writeObject(o);
			encoder.finish();
		}
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
		map =(Galaxy) decoder.readObject();
		decoder.close();
		
		if(SAVE)
		{
			FileWriter FW = new FileWriter("C:\\Users\\David\\Desktop\\network_test.xml");
			FW.write(str.toString(),0,str.length());
			FW.close();
		}
	}
	
	public void loadMap(File f) throws FileNotFoundException, ClassCastException, NullPointerException //for the server
	{
		XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
		map = (Galaxy)d.readObject();
		d.close();
		
		if(!(map instanceof Galaxy)) //BOOKMARK!  NEEDS BETTER VALIDITY TESTS.  empty galaxies still pass this test... but it is a start.
			throw new NullPointerException();
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
				Boolean connected=true;
				while(!Thread.interrupted() && connected)
				{
					System.out.println("reading object...");
					String line="";
					StringBuffer str = new StringBuffer("");
					
					Boolean kill=false;
					while(!kill)
					{
						str.append(line);
						if(line.indexOf("</java>") == -1)
						{
							line = br.readLine();
							if(line==null)
							{
								kill=true;
								connected=false;
							}
						}
						else
							kill=true;
					}
					
					if(connected)
					{
						ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
						XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
						Order o =(Order) decoder.readObject(); //TODO: add in leaving message, which will need to be handled differently here
						decoder.close();
						System.out.println("\t" + o.getClass().getName());
						pending_execution.add(o);
					}
				}
				
				if(!connected)
					JOptionPane.showMessageDialog(GI.frame, "Connection Lost.", "Error", JOptionPane.ERROR_MESSAGE);
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
	
	public void endAllThreads()
	{
		if(serverThread instanceof Thread)
			serverThread.interrupt();
		if(lobbyThread instanceof Thread)
		{
			leavingLobby();
			lobbyThread.interrupt();
		}
		if(readThread instanceof Thread)
		{
			//BOOKMARK!  send leaving game message
			readThread.interrupt();
		}
		if(startThread instanceof Thread)
			startThread.interrupt(); //BOOKMARK - in this case, the other player needs to be able to detect if the person left the game.  perhaps modify this to send a notification, and StartGame function to recieve it.
		if(TC instanceof TimeControl)
			TC.stopTask();
		endConnection();
	}
	
	//***************************************************************************These next few methods deal with in-game updating
	
	private class Updater extends TimerTask
	{
		private Updater(){}
		
		public void run()
		{
			try
			{
				updateGame();
			}
			catch (DataSaverControl.DataNotYetSavedException e)
			{
				//TODO: work on exception handling
				e.printStackTrace();
				throw new RuntimeException();
			}
			//System.out.println("update!!" + Long.toString(TC.getTime()));
		}
	}
	
	public void updateGame() throws DataSaverControl.DataNotYetSavedException
	{
		long time_elapsed=TC.getTime();
		long update_to=TC.getLast_time_updated();
		//System.out.println("Updating to time_elapsed=" + Long.toString(time_elapsed));
		//start events that need to occur before time_elapsed
		
		//can safely use unsynchronized version here since this is only used by the current thread
		PriorityQueue<Order> local_pending_execution = new PriorityQueue<Order>();
		
		do
		{
			Order o = pending_execution.peek();
			if(o != null && o.scheduled_time <= time_elapsed)
				local_pending_execution.add(pending_execution.remove()); //if this does not remove o, it removes one just inserted earlier than o.
			else
				break; /*small chance an order should be removed and executed this time around but isn't, 
						if we don't see it with peek.  it will be executed next time through updateGame,
						though with a bit of reversion*/
		} while(true);
		
		/*now figure out the earliest order in local_pending_execution, and make that time update_to,
		 * if update_to is currently larger*/
		Order first_order = local_pending_execution.peek();
		if(first_order != null)
		{
			long next_order_time = first_order.scheduled_time;
			if(next_order_time < update_to)
				update_to = next_order_time; //forces the main loop to reconsider below
		}
		
		
		//update data in all systems
		for(GSystem sys : map.systems)
		{
			//move all planets
			for(Satellite<?> sat : sys.orbiting)
			{
				if(sat instanceof Planet)
				{
					for(Satellite<?> sat2 : ((Planet)sat).orbiting)
					{
						sat2.orbit.move(time_elapsed);
					}
				}
				sat.orbit.move(time_elapsed);
			}
		}
		
		//update all planets, facilities, ships and missiles
		for(; update_to <= time_elapsed; update_to+=GalacticStrategyConstants.TIME_GRANULARITY)
		{
			Order o;
			while( (o = local_pending_execution.peek()) != null && o.scheduled_time <= update_to)
			{
				/**execute does all the necessary reversion itself.  It never reverts anything to earlier than
				 * scheduled_time, which should be within one time grain less than update_to*/
				local_pending_execution.addAll(local_pending_execution.remove().execute(map));
			}
			
			/**update all intersystem data.  This is must be within the loop in case ships are reverted back
			 * into warp or something of the sort*/
			for(int i=0; i<players.length; i++)
			{
				if(players[i] != null)
				{
					Iterator<Ship> ship_it = players[i].ships_in_transit.iterator();
					Ship s;
					while(ship_it.hasNext())
					{
						s=ship_it.next();
						s.moveDuringWarp(update_to, ship_it); //the iterator is passed so that moveDuringWarp can remove the ship from the iteration, and by doing so from ships_in_transit
					}
				}
			}
			
			for(GSystem sys : map.systems)
			{
				/*We must stick facilities and planets in this loop because otherwise Ship updating would not
				be coordinated with facilities and planets, so bugs could then occur in terms of building ships
				or invading planets.
				
				For instance, consider this scenario: ship is attacking a Shipyard, and Shipyard is about to
				complete a new Ship right around the time it is about to explode.  If it should finish
				the ship after it is destroyed, but a call to updateGame must handle both the time in which
				the Shipyard will be destroyed and in which the ship would be completed if the shipyard were
				not destroyed, and if facilities/planets were updated before the Ships all the way to
				time_elapsed, then the Shipyard could be updated to a time later than the time it is
				destroyed at, produce the ship which it should not produce, and then be destroyed.  But now
				we have a ship which should have not completed construction.   Uh oh...
				
				Though less dramatic, similar issues can exist with mining/taxation.  So it all goes in here.*/
				
				//update planets/facilities:
				for(Satellite<?> sat : sys.orbiting)
				{
					if(sat instanceof Planet)
					{
						((Planet)sat).update(update_to);
						for(Satellite<?> sat2 : ((Planet)sat).orbiting)
						{
							if(sat2 instanceof Moon)
							{
								((Moon)sat2).update(update_to);
							}
						}
					}
				}
				
				//update data for all ships
				for(int i=0; i<sys.fleets.length; i++)
				{
					synchronized(sys.fleets[i].lock)
					{
						Fleet.ShipIterator ship_iteration = sys.fleets[i].iterator();
						for(Ship.ShipId j; ship_iteration.hasNext();)
						{
							j=ship_iteration.next();
							sys.fleets[i].ships.get(j).update(update_to, ship_iteration);
						}
					}
				}
				
				//NOTE: Missile collision detection relies on Missiles being updated after ships.  See Missile.collidedWithTarget
				//update all missiles AND save data
				synchronized(sys.missiles)
				{
					Iterator<Missile.MissileId> missile_iteration = sys.missiles.keySet().iterator();
					for(Missile.MissileId i; missile_iteration.hasNext();)
					{
						i=missile_iteration.next();
						sys.missiles.get(i).update(update_to, missile_iteration); //returns true if the missile detonates
					}
				}
			}
		}
		
		TC.setLast_time_updated(update_to);
		
		if(!local_pending_execution.isEmpty())
		{
			System.out.println("We still have orders in the local queue.  should not be possible!");
			pending_execution.addAll(local_pending_execution);
		}
		
		SwingUtilities.invokeLater(new InterfaceUpdater(time_elapsed));
	}
	
	public class InterfaceUpdater implements Runnable
	{
		long time;
		
		public InterfaceUpdater(long t){time=t;}
		
		public void run()
		{
			updateInterface(time);
		}
	}
	
	public void updateInterface(long time_elapsed)
	{
		GI.update();
			
		if(GI.sat_or_ship_disp == GameInterface.SAT_PANEL_DISP)
			GI.SatellitePanel.update(time_elapsed);
		else if(GI.sat_or_ship_disp == GameInterface.SHIP_PANEL_DISP)
			GI.ShipPanel.update();
		GI.time.setText("Time: " + Long.toString(time_elapsed/1000));
		GI.metal.setText("Metal: "+Long.toString(new Double(players[player_id].metal).longValue()));
		GI.money.setText(GI.indentation + "Money: "+Long.toString(new Double(players[player_id].money).longValue()));
		GI.redraw();
	}
}