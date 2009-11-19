import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

public class GameControl
{
	TimeControl TC;
	Player player;
	Galaxy map;
	ServerSocket the_server_socket; 
	Socket the_client_socket ;
	boolean hosting;    								//true if running as server  false if running as client
	
	HashSet<Event> events_pending;
	
	
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
		TC = new TimeControl();
	}
	
	public void updateGame()
	{
		long time_elapsed=TC.getTime();
		
		//start events that need to occur before time_elapsed
		
		for(Event e: events_pending)
		{
			if(e.scheduled_time >= time_elapsed)
			{
				e.run();
				events_pending.remove(e);
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
	
	public void host()
	{
		the_server_socket= null;
		try {
		    the_server_socket = new ServerSocket(7007);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: 7007");
		}
		
		the_client_socket=null;
		try {
		    the_client_Socket = the_server_socket.accept();
		} catch (IOException e) {
		    System.out.println("Accept failed: 7007");
		}
		
	}
	
	public void joinAsClient() throws UnknownHostException
	{
		the_client_socket=null;
        
        String ip_in_string;                                            
        ip_in_string=JOptionPane.showInputDialog("Enter IP address");
        byte[] ip_in_byte=new byte[4];
        String[] ip=ip_in_string.split(".");              
        for (int i=0; i<=3; i++)
      	  ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);      
        InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
        
        try {
        	the_client_socket = new Socket(ipaddress, 7007);                     
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to the host");
        }
	}
	
	public void notifyAllPlayers(Event e)
	{
		try
		{
			if (hosting)
			{
				XMLEncoder encoder = new XMLEncoder((the_client_socket).getInputStream());
				encoder.writeObject(e);
				encoder.close();
			}
			else
			{
				
			}
		}
		catch(IOException x)
		{
			//no socket open? shouldn't happen
		}
	}
}