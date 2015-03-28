package galactic_strategy;

import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.Galaxy;
import galactic_strategy.game_objects.OwnableSatellite;
import galactic_strategy.sync_engine.GameUpdater;
import galactic_strategy.sync_engine.Message;
import galactic_strategy.sync_engine.TimeControl;
import galactic_strategy.testing.RecordKeeper;
import galactic_strategy.testing.SimulateAction;
import galactic_strategy.testing.SimulatedTimeControl;
import galactic_strategy.ui.GameInterface;
import galactic_strategy.ui.GameLobby;
import galactic_strategy.ui.GameStartupDialog;
import galactic_strategy.ui.Resources;
import galactic_strategy.user_actions.Order;

import java.awt.Color;
import java.beans.*;
import java.net.*;
import java.io.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public strictfp class GameControl
{
	static final int DEFAULT_PORT_NUMBER = Constants.DEFAULT_PORT_NUMBER;
	static final String LEFT_LOBBY_MSG = "Im leaving the lobby.";
	static final String MAP_CHOSEN = "Host chooses map::"; //do not change ending
	static final String READY_MSG = "I'm ready, let's start!";
	static final String NOT_READY_MSG = "wait, I take that back, I'm not ready";
	static final String START_MSG = "START THE GAME";
	
	public GameUpdater updater;
	int player_id;
	Player[] players;
	public Galaxy map;
	public RecordKeeper record_keeper; //This is for testing

	JFileChooser filechooser; //for single player testing
	
	public GameInterface GI;
	GameLobby GL;
	GameStartupDialog GSD;
	
	volatile ServerSocket the_server_socket; 
	volatile Socket the_socket;
	volatile OutputStream OS;
	volatile BufferedReader input_reader;
	
	Thread serverThread; //waits for connections
	Thread lobbyThread; //reads data and updates the GameLobby
	Thread readThread; //reads data during the game
	public Thread startThread; //processes start game.   This is a separate Thread because it can crash the interface if run on swing's event thread
	
	public GameControl(GameInterface gi, RecordKeeper r)
	{
		GI = gi;
		record_keeper = r;
		try {
			Resources.preload(); //preload images
		} catch (IOException e) {
			System.out.println("trouble reading images");
			e.printStackTrace();
		}
		
		players = new Player[Constants.MAX_PLAYERS];
		map=new Galaxy();
		
		//preload file chooser for singlePloyerTest map loading
		filechooser = new JFileChooser();
		filechooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));
		updater = new GameUpdater(this);
	}
	
	public GameControl()
	{
		try {
			Resources.preload(); //preload images
		} catch (IOException e) {
			System.out.println("trouble reading images");
			e.printStackTrace();
		}
	}
		
	public Player createThePlayer(boolean hosting) throws CancelException
	{
		return Player.createPlayer(hosting);
	}
	
	public int nextAvailableID()
	{
		for(int i=0; i<players.length; i++){
			if(!(players[i] != null))
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
			if(players[i] != null)
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
		if(GSD != null)
			GSD.constructDialog();
		else
			GSD = new GameStartupDialog(GI.getFrame(), this);
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
		
		//TODO: This next block is commented out since it is not yet necessary.  Once one player joins
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
			
			if(players[player_id].isHosting())
			{
				System.out.println("host start sequence begin");
				
				writer.println(START_MSG);
				
				//File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
				boolean retry=true;
				while(retry)
				{
					if(!Thread.interrupted()){
						if(input_reader.ready()){
							input_reader.readLine();//wait for client
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
				long[] pingtimes = new long[1000];
				long pongtime=0;
				String pongmsg="";
				
				for(int i=0; !pong && i<1000; i++)
				{
					writer.println("ping" + Integer.toString(i));
					pingtimes[i] = System.nanoTime();
					System.out.println("ping" + Integer.toString(i));
					//pong
					if(input_reader.ready()) {
						pongmsg = input_reader.readLine();
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
				int pong_num = Integer.parseInt(pongmsg);
				int offset_estimate = (int)((pongtime-pingtimes[pong_num])/2);
				System.out.println("Offset estimated: " + Integer.toString(offset_estimate) + " from pong " + pong_num);
				
				//send offset_estimate
				writer.println(Integer.toString(offset_estimate));
				
				//Start time
				updater.setTimeManager(new TimeControl(0));
				
				//send start signal
				writer.println("Start");
			}
			else
			{
				System.out.println("Client start sequence initiated.");

				writer.println("I'm ready!");//helps make sure the map is sent safely
				downloadAndLoadMap(false);
				System.out.println("map loaded");
				
				//return message for offset estimate
				System.out.println("waiting for ping");
				String pingmsg= input_reader.readLine(); //impractical to make this non-blocking, since it will mess up timing measure
				System.out.println("pong time");
				
				if(pingmsg == null) //only if the connection is closed
					throw new IOException();
				
				writer.println(pingmsg.substring(4));
				System.out.println("pong!");
				
				//receive offset_estimate
				String received="";
				do
				{
					boolean new_line_read=false;
					while(!new_line_read){ //readLine in a nonblocking manner, ending startGame on interrupts
						if(!Thread.interrupted()){
							if(input_reader.ready()){
								received=input_reader.readLine();
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
				
				//start time when start signal is received
				input_reader.readLine();
				updater.setTimeManager(new TimeControl(offset));
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
			endConnection();
			return;
		}
		
		//start event reading
		readThread = new Thread(new EventReader());
		readThread.start();
		
		gameSetup();
		
		//start game graphics...
		GI.drawGalaxy(GameInterface.GALAXY_STATE.NORMAL);
		
		//set game to update itself
		updater.startUpdating();
	}
	
	private void gameSetup()
	{
		//set up systems for the game
		for(GSystem sys : map.getSystems())
			sys.setUpForGame(this);
		
		//start all players in assigned locations
		for (int i = 0; i < players.length; i++)
		{
			if (players[i] != null)
			{
				System.out.println("setting up player" + i);
				OwnableSatellite<?> sat = map.getStart_locations().get(i);
				sat.setAsStartLocation(players[i]);
			}
		}
		
		map.saveOwnablesData(0);
	}
	
	public void startGameViaThread()
	{
		if(GL != null)
			GL.dispose();
			
		startThread = new Thread(new Runnable(){public void run(){startGame();}});
		startThread.start();
	}
	
	public void startTest(int num_players, boolean automate, File map_file)
	{
		if(!automate)
		{
			try{
				Player the_player = createThePlayer(true);
				player_id=0;
				the_player.setId(0);
				the_player.setColor(Color.GREEN);
				players[0] = the_player;
			} catch(CancelException e){
				startupDialog();
				return;
			}
		}
		else
		{
			for(int i=0; i<num_players; ++i)
			{
				players[i] = new Player("Player"+i, i==0); //Player0 is always the host
				players[i].setColor(Constants.DEFAULT_COLORS[i]);
				players[i].setId(i);
			}
		}
		
		//CHOOSE AND LOAD MAP
		//load the map.  notify if errors.  This is supposed to validate the map by attempting to load it
		
		boolean map_loaded = false;
		boolean map_valid = (map_file != null);
		while(!map_loaded)
		{
			try{
				try{
					//stolen from GameLobby.actionPreformed
					if(!map_valid)
					{
						int val = filechooser.showOpenDialog(GI.getFrame());
						if(val==JFileChooser.APPROVE_OPTION){
							map_file = filechooser.getSelectedFile();
						} else {
							startupDialog();
							return;
						}
					}
					loadMap(map_file); //parsing errors render the map invalid, causing one of the messages in the catch statements.
				} catch(MapLoadException mle)
				{
					handleError("Map Load Error",	"The map was loaded, but exceptions occured during the load,\n" +
													"which most likely means that the map is not compatible with\n" +
													"this version of the game. Attempting to use the map anyway...", !automate);
				}
				
				//the existence of the name is the second line of defense.
				if(!(map.getName() != null)){
					map=null;
					
					handleError("Map Load Error", "The selected file is not a completed map.  Please pick a different map.", !automate);
				}
				else
					map_loaded=true;
				
			} catch(FileNotFoundException fnfe) {
				handleError("Error - File not Found", "The file was not found.  Please choose another file.", !automate);
				map_valid=false;
			} catch(ClassCastException cce) {
				handleError("Map Load: Class Casting Error", "The file you have selected is not a map", !automate);
			} catch(NullPointerException npe) {
				handleError("Map Load Error", "Map loading failed.  The selected file is not a valid map.", !automate);
			}
		}
		
		try
		{
			gameSetup();
			
			if(!automate)
			{
				updater.setTimeManager(new TimeControl(0));
				//display the Galaxy
				GI.drawGalaxy(GameInterface.GALAXY_STATE.NORMAL);
			
				//set game to update itself
				updater.startUpdating();
			}
			else
			{
				updater.setupMostRecentTime();
				updater.setTimeManager(new SimulatedTimeControl());
			}
		} catch(Exception e)
		{
			handleError("Start game failed", "Start game has failed, potentially due to a MapLoadException.", !automate);
			e.printStackTrace();
			if (!automate)
				startupDialog();
			return;
		}
	}
	
	public void handleError(String header, String message, boolean do_gui)
	{
		if(do_gui) {
			JOptionPane.showMessageDialog(GI.getFrame(), message, header, JOptionPane.ERROR_MESSAGE);
		}
		else {
			System.err.println(header + ": " + message);
		}
	}
	
	public void host() //creates new thread to host the game on
	{
		try{
			Player the_player = createThePlayer(true);
			player_id=0;
			the_player.setId(0);
			players[0] = the_player;
		} catch(CancelException e){
			startupDialog();
			return;
		}
		
		GL=new GameLobby(GI.getFrame(), this);
		serverThread = new Thread(new HostRunnable());
		serverThread.start();
	}
	
	public void endHost() //closes down the serverThread, which is responsible for listening for players trying to join
	{
		serverThread.interrupt();
	}
	
	public class HostRunnable implements Runnable
	{
		public HostRunnable() {
		}
		
		public void run()
		{
			System.out.println("Host runnable running...");
			
			// close if already exists
			try {
				if(the_server_socket != null)
					the_server_socket.close();
			}
			catch(IOException e){}
			
			// open new socket
			try{the_server_socket = new ServerSocket(DEFAULT_PORT_NUMBER);}
			catch (IOException e)
			{
				System.out.println("Could not listen on port" + Integer.toString(DEFAULT_PORT_NUMBER)+".  Hosting failed.");
				GL.leaveGame(false);
				return;
			}
			
			try
			{
				if(the_socket != null)
					the_socket.close();
			}
			catch(IOException e){}
			
			try {
				the_server_socket.setSoTimeout(500);
				boolean go=true;
				while(!Thread.interrupted() && go)
				{
					try
					{
						System.out.println("Waiting for connections...");
						setUpIOStreams(the_server_socket.accept());
						go=false;
						
						//send player roster.  start with the number of players, and then send the_player and then go through players hashset
						PrintWriter w = new PrintWriter(OS, true); //this should auto-flush
						
						System.out.println("Sending # of players");
						//r.readLine(); //wait until client is ready
						int num_players = players.length;
						w.println(Integer.toString(num_players));
						
						for(int i=0; i<num_players; i++){
							if(players[i] != null){
								w.println(players[i].getName());
								w.println(Integer.toString(i));
								w.println(Boolean.toString(players[i].ready));
							} else {
								w.println("skip player>>");
							}
						}					
						
						//request name
						String name;
						name=input_reader.readLine();
						Player p = new Player(name, false);
						
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
		
		try {
			if(the_socket != null)
				the_socket.close();
		}
		catch(IOException e){}
		
		byte[] ip_in_byte = new byte[4];
		boolean ip_valid=false;
		while(!ip_valid) {
			try {
				String ip_in_string=JOptionPane.showInputDialog("Enter the IP address of the host:", Constants.DEFAULT_IP);
				if(ip_in_string != null){
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
				JOptionPane.showMessageDialog(GI.getFrame(), "This is not a valid IP address.  Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		Player the_player;
		try{
			the_player = createThePlayer(false);
		} catch(CancelException e){
			startupDialog();
			return;
		}
		
		try
		{	
			InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
			//InetAddress ipaddress=InetAddress.getLocalHost();
			setUpIOStreams(new Socket(ipaddress, DEFAULT_PORT_NUMBER));
			
			//receive other players' names and id's
			PrintWriter pw = new PrintWriter(OS, true); //this version of the constructor sets up automatic flushing
			
			int num_players = Integer.parseInt(input_reader.readLine());
			for(int i=0; i<num_players; i++){
				String name_input = input_reader.readLine();
				if(!name_input.equals("skip player>>")) {
					Player p = new Player(name_input, false);
					p.setId(Integer.parseInt(input_reader.readLine()));
					p.setReady(Boolean.parseBoolean(input_reader.readLine()));
					players[p.getId()] = p;
				}
			}
			
			//send name
			pw.println(the_player.getName());
			
			//receive player id number
			player_id = Integer.parseInt(input_reader.readLine());
			the_player.setId(player_id);
			players[player_id] = the_player;
			
			System.out.println("Connection Established");
			
			//display lobby.  This order is important.  The lobby must be created before we create the updater for it, since the Host immediately fires off a message containing the current choice of map, but framed as an update
			GL = new GameLobby(GI.getFrame(), this);
			setUpLobbyUpdater();
		}
		catch (UnknownHostException e)
		{
			System.err.println("Unknown host");
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to the host");
			JOptionPane.showMessageDialog(GI.getFrame(), "The specified IP could not be reached.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
		}
		catch(NumberFormatException e)
		{
			System.err.println("Connection lost");
			JOptionPane.showMessageDialog(GI.getFrame(), "Connection Lost.", "Error", JOptionPane.ERROR_MESSAGE);
			endConnection();
			startupDialog();
		}
	}
	
	private void updateGL()
	{
		GL.updateNames();
		GL.maybeEnableStartGame();
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
			//TODO: this only works for one other player
			try {
				while(!Thread.interrupted()){
					if(input_reader.ready()) {
						String notification = input_reader.readLine();
						if(notification != null)
						{
							String[] split_notification = notification.split(":");
							if(notification.indexOf(":")!= -1 && split_notification[1].equals(LEFT_LOBBY_MSG)) {
								playerLeft(Integer.parseInt(split_notification[0]));
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
								GL.maybeEnableStartGame();
							}
							else if(notification.equals(START_MSG))
							{
								startGameViaThread(); //must start on a different thread because start game terminates this one.  If start game ran on this thread, it would end itself.
							}
						}
						else
						{
							//TODO: figure out who left, in multiplayer game.  right now it suffices to say
							//that the other player left, since there is only one.
							
							int other_player_id = -1;
							for(int i=0; i<players.length; ++i)
							{
								if(players[i] != null && player_id != i)
									other_player_id = i;
							}
							
							playerLeft(other_player_id);
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
		
		private void playerLeft(int id_leaving)
		{
			if(!players[id_leaving].isHosting()){
				players[id_leaving]=null;
				updateGL();
				serverThread = new Thread(new HostRunnable());
				serverThread.start();
				return;
			} else { //TODO: this assumes if you aren't hosting, the other player is - i.e. 2 player assumption
				JOptionPane.showMessageDialog(GI.getFrame(), "The host has left the game.", "Host Left", JOptionPane.INFORMATION_MESSAGE);
				GL.leaveGame(false);
				return;
			}
		}
	}
	
	public void leavingLobby() //responsible for informing other players of the user leaving the game.
	{
		if(OS != null)
		{
			PrintWriter w = new PrintWriter(OS, true);
			w.println(Integer.toString(player_id) +":" + LEFT_LOBBY_MSG);
			players = new Player[Constants.MAX_PLAYERS];
		}
	}
	
	public void declareReady() //TODO: in a 3+ player game, this function will need to be modified to notify ALL players in the game
	{
		if(OS != null)
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
		if(OS != null)
		{
			PrintWriter w = new PrintWriter(OS, true);
			w.println(MAP_CHOSEN + GL.map_label.getText());
		}
	}
	
	private void setUpIOStreams(Socket s) throws IOException
	{
		the_socket = s;
		input_reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		OS = s.getOutputStream();
	}
	
	public void notifyAllPlayers(Order o)
	{
		//notify other players
		if(OS != null)
		{
			synchronized(OS) //Synchronize to prevent race conditions on writing to the socket
			{
				XMLEncoder2 encoder = new XMLEncoder2(OS);
				encoder.writeObject(new Message(Message.Type.ORDER, o));
				encoder.finish();
			}
		}

	}
	
	public void notifyAllPlayersOfDecision(Order o)
	{
		//notify other players
		if(OS != null)
		{
			synchronized(OS) //Synchronize to prevent race conditions on writing to the socket
			{
				XMLEncoder2 encoder = new XMLEncoder2(OS);
				encoder.writeObject(new Message(Message.Type.DECISION, o));
				encoder.finish();
			}
		}		
		
		// for simulations
		if (record_keeper != null)
			record_keeper.checkDecision(o);
	}
	
	public void downloadAndLoadMap(boolean SAVE) throws IOException //for the client
	{
		String line=input_reader.readLine();
		StringBuffer str = new StringBuffer("");
		Boolean kill=false;
		while(line != null && !kill)
		{
			str.append(line);
			if(line.indexOf("</java>") == -1)
				line = input_reader.readLine();
			else
				kill=true;
		}
		
		ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
		XMLErrorDetector error_checker = new XMLErrorDetector();
		decoder.setExceptionListener(error_checker);
		map =(Galaxy) decoder.readObject();
		decoder.close();
		
		if(error_checker.isError())
		{
			JOptionPane.showMessageDialog(GI.getFrame(), "Warning: exceptions occured while loading the map.  The map is probably not a valid file.", "Map Load Exception", JOptionPane.ERROR_MESSAGE);
		}
		
		if(SAVE)
		{
			FileWriter FW = new FileWriter("C:\\Users\\David\\Desktop\\network_test.xml");
			FW.write(str.toString(),0,str.length());
			FW.close();
		}
	}
	
	/**for the server*/
	public void loadMap(File f) throws FileNotFoundException, ClassCastException, NullPointerException, MapLoadException
	{
		XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
		XMLErrorDetector error_check = new XMLErrorDetector();
		d.setExceptionListener(error_check);
		map = (Galaxy)d.readObject();
		d.close();
		
		if(map == null)
			throw new NullPointerException();
		if(error_check.isError())
			throw new MapLoadException();
	}
	
	public static class MapLoadException extends Exception
	{
		private static final long serialVersionUID = -8337192629730659561L;
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
							line = input_reader.readLine();
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
						System.out.println(str);
						ByteArrayInputStream sr = new ByteArrayInputStream(str.toString().getBytes("UTF-8"));
						XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(sr));
						Message m =(Message) decoder.readObject(); //TODO: add in leaving message, which will need to be handled differently here
						decoder.close();
						
						Order o = m.contents;
						
						System.out.println("\t" + m.getType() + " " + m.contents.getClass().getName());
						
						//atomically queue and log the new Message
						synchronized(updater.log_lock)
						{
							switch(m.getType())
							{
								case ORDER:
								{
									updater.scheduleOrder(o);
									
									//TODO: move debugging code
									long time = updater.getTime();
									updater.log(new SimulateAction(time, o,
											SimulateAction.ACTION_TYPE.SCHEDULE_ORDER,
											SimulateAction.ORDER_TYPE.REMOTE));
									break;
								}
								case DECISION:
								{
									updater.decideOrder(m);
									
									//TODO: move debugging code
									//TODO: I think we should log who this came from... oh yeah, only two player at the moment
									long time = updater.getTime();
									updater.log(new SimulateAction(time, o,
											SimulateAction.ACTION_TYPE.RECEIVED_DECISION,
											SimulateAction.ORDER_TYPE.REMOTE));
									break;
								}
							}
						}
					}
				}
				
				if(!connected)
					JOptionPane.showMessageDialog(GI.getFrame(), "Connection Lost.", "Error", JOptionPane.ERROR_MESSAGE);
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
		try{
			if(OS != null)
				OS.close();
		}catch(IOException e){e.printStackTrace();}
		try{
			if(input_reader != null)
				input_reader.close();
		}catch(IOException e){e.printStackTrace();}
		try{
			if(the_socket != null)
				the_socket.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public void endAllThreads()
	{
		if(serverThread != null)
			serverThread.interrupt();
		if(lobbyThread != null)
		{
			leavingLobby();
			lobbyThread.interrupt();
		}
		if(readThread != null)
		{
			//TODO: send leaving game message
			readThread.interrupt();
		}
		if(startThread != null)
			startThread.interrupt(); //TODO: in this case, the other player needs to be able to detect if the person left the game.  perhaps modify this to send a notification, and StartGame function to receive it.
		if(updater != null)
		{
			updater.stopUpdating();
		}
		endConnection();
	}
	
	/**scheduleOrder
	 * 
	 * calls GameUpdater.scheduleOrder and notifyAllPlayers
	 * i.e. should be used to schedule an order AND tell other computers too
	 * 
	 * @param o the order to schedule for execution
	 */
	public void scheduleOrder(Order o)
	{
		//atomically queue and log current user's order
		//TODO: move debugging code
		synchronized(updater.log_lock)
		{
			updater.log(new SimulateAction(updater.getTime(), o,
					SimulateAction.ACTION_TYPE.SCHEDULE_ORDER,
					SimulateAction.ORDER_TYPE.LOCAL));
			
			updater.scheduleOrder(o);
			
			notifyAllPlayers(o);
		}
	}
	
	public void updateInterface(long time_elapsed)
	{
		//if statement only necessary for GameSimulator
		if(GI != null)
		{
			GI.update(time_elapsed);
			
			switch(GI.displayed_control_panel)
			{
				case SAT_PANEL:
					GI.SatellitePanel.update(time_elapsed);
					break;
				case SHIP_PANEL:
					GI.ShipPanel.update();
					break;
				case SYS_PANEL:
					GI.SysPanel.update(time_elapsed);
					break;
				case FORMATION_PANEL:
					// TODO this should probably do something
					break;
				case NONE:
					break;
			}

			GI.updateStats(time_elapsed, Math.round(players[player_id].getMoney()), Math.round(players[player_id].getMetal()));
			GI.redraw();
		}
	}
}