import javax.swing.JFrame;
import java.util.*;
import java.beans.*;
import java.net.*;
import java.io.*;

public class GameControl
{
	TimeControl TC;
	Player player;
	Galaxy map;
	Socket the_socket; 
	
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
			//no socket open? shouldn't happen
		}
	}
}