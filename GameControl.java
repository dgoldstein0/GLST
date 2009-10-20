import javax.swing.JFrame;
import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

public class GameControl
{
	long time_elapsed; //can support over 292 years
	long start_time; 
	Player player;
	Galaxy map;
	Socket the_socket; 
	
	HashSet<Event> events_pending;
	
	
	public GameControl(JFrame frame)
	{
		time_elapsed=0;
		player=Player.createPlayer();
		map=new Galaxy();
	}
	
	public GameControl(){}
	
	public Player getPlayer(){return player;}
	public void setPlayer(Player p){player=p;}
	public long gettime_elapsed(){return time_elapsed;}
	public void settime_elapsed(long t){time_elapsed=t;}
	public Galaxy getMap(){return map;}
	public void setMap(Galaxy g){map=g;}
	
	public void startGame()
	{
		start_time=System.nanoTime();
		time_elapsed=0;
	}
	
	public void updateTime()
	{
		time_elapsed=System.nanoTime()-start_time;
		
		//start events that need to occur before time_elapsed
		
		for(Event e: events_pending)
		{
			if(e.scheduled_time >= time_elapsed)
			{
				e.execute();
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
	
	public void notifyAllPlayers(Event e)
	{
		try
		{
			XMLEncoder encoder = new XMLEncoder(the_socket.getOutputStream());
			encoder.writeObject(e);
			encoder.close();
		}
		catch(IOException x)
		{
			//no socket open?
		}
	}
}