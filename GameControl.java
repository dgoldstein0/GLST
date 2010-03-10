import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;
import javax.swing.SwingUtilities;

import javax.swing.JFileChooser;
import java.awt.Color;
import javax.swing.filechooser.FileNameExtensionFilter;


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

	GameControl GC = this;
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
	Thread startThread; //processes start game.   Because it can crash the interface if run on swing's event thread
	
	Set<Event> pending_execution = Collections.synchronizedSet(new HashSet<Event>());
	
	public GameControl(GameInterface gi)
	{
		GI = gi;
		GalacticStrategyConstants.ImageLoader();
		players = new Player[GalacticStrategyConstants.MAX_PLAYERS];
		map=new Galaxy();
	}
	
	public GameControl(){GalacticStrategyConstants.ImageLoader();}
		
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
		for(int i=0; i<GC.players.length; i++){
			if(GC.players[i] instanceof Player)
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
			GC.endConnection();
			return;
		}
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
				
				writer.println(START_MSG);
				
				//File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
				boolean retry=true;
				while(retry)
				{
					if(!Thread.interrupted()){
						if(reader.ready()){
							reader.readLine();//wait for client BMM
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
			sys.setUpForGame();
		
		//start everyone in assigned locations
		for(int i=0; i<map.start_locations.size(); i++)
		{
			Planet p = map.start_locations.get(i);
			p.setOwner(players[i]);
			p.facilities.add(new Base((long)0));
		}
		
		//start game graphics...
		GI.drawGalaxy();
		
		//set game to update itself
		TC.startConstIntervalTask(new Updater(),5);
	}
	
	public void startGameViaThread()
	{
		startThread = new Thread(new Runnable(){public void run(){GC.startGame();}});
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
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));
		
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
			} catch(ClassCastException cce) {
				JOptionPane.showMessageDialog(GI.frame, "The file you have selected is not a map", "Class Casting Error", JOptionPane.ERROR_MESSAGE);
			} catch(NullPointerException npe) {
				JOptionPane.showMessageDialog(GI.frame, "Map loading failed.  The selected file is not a valid map.", "Map Load Error", JOptionPane.ERROR_MESSAGE);
			}
			
			TC = new TimeControl(0);
			
			//set up systems for the game
			for(GSystem sys : map.systems)
				sys.setUpForGame();
			
			//start the player in assigned location
			Planet p = map.start_locations.get(player_id);
			p.setOwner(players[player_id]);
			p.facilities.add(new Base((long)0));
			
			//display the Galaxy
			GI.drawGalaxy();
			
			//set game to update itself
			TC.startConstIntervalTask(new Updater(),5);
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
								GC.updateGL();
							}
						});
						
						GC.setUpLobbyUpdater(); //THIS WILL BE AN ISSUE FOR 3+ PLAYER GAMES.  ESPECIALLY IF ONLY 1 LobbyUpdater used, which checks for msgs from all.  Then we cannot let LobbyUpdater check for updates before player is done being set up
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
			GC.setUpLobbyUpdater();
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
								GC.updateGL();
								GC.serverThread = new Thread(new HostRunnable());
								GC.serverThread.start();
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
							GC.updateGL(); //this function takes care of enabling/disabling start button for us
						} else if(notification.indexOf(":")!= -1 && split_notification[1].equals(NOT_READY_MSG)) {
							//only the host should recieve this message
							int id_ready = Integer.parseInt(split_notification[0]);
							players[id_ready].ready=false;
							GC.updateGL(); //this function takes care of enabling/disabling start button for us
						}
						else if(notification.indexOf(MAP_CHOSEN) != -1)
						{
							GL.map_label.setText(notification.split("::")[1]);
							GL.start_game.setEnabled(GL.readyToStart()); //check to see if we are ready to start the game, and enable start button if so.
						}
						else if(notification.equals(START_MSG))
						{
							GC.startGameViaThread(); //must start on a different thread because start game terminates this one.  If start game ran on this thread, it would end itself.
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
			updateGame();
			//System.out.println("update!!" + Long.toString(TC.getTime()));
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
				sat.orbit.move(time_elapsed);
				if(sat instanceof Planet)
				{
					players[player_id].changeMoney(((Planet)sat).updatePopAndTax(time_elapsed));
					((Planet)sat).updateConstruction(GI, time_elapsed);
					for(Facility f : ((Planet)sat).facilities)
					{
						f.updateStatus(time_elapsed);
					}
					for(Satellite sat2 : ((Planet)sat).satellites)
					{
						sat2.orbit.move(time_elapsed);
						if(sat2 instanceof Moon)
						{
							players[player_id].changeMoney(((Moon)sat2).updatePopAndTax(time_elapsed));
							((Moon)sat2).updateConstruction(GI, time_elapsed);
							for(Facility f : ((Moon)sat2).facilities)
								f.updateStatus(time_elapsed);
						}
					}
				}
			}

		}
		
		GI.update();
		
		if(GI.sat_or_ship_disp == GameInterface.SAT_PANEL_DISP)
			GI.SatellitePanel.update(time_elapsed);
		GI.time.setText("Time: " + Long.toString(time_elapsed/1000));
		GI.metal.setText("Metal: "+Long.toString(new Double(players[player_id].metal).longValue()));
		GI.money.setText(GI.indentation + "Money: "+Long.toString(new Double(players[player_id].money).longValue()));
		GI.redraw();
	}
}