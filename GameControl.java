import javax.swing.JFrame;

public class GameControl
{
	long time_elapsed;
	Player player;
	Galaxy map;
	
	public GameControl(JFrame frame)
	{
		time_elapsed=0;
		player=Player.createPlayer();
		map=new Galaxy();
	}
	
	public GameControl(){}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public void setPlayer(Player p)
	{
		player=p;
	}
	
	public long gettime_elapsed()
	{
		return time_elapsed;
	}
	
	public void settime_elapsed(long t)
	{
		time_elapsed=t;
	}

	public Galaxy getMap()
	{
		return map;
	}
	
	public void setMap(Galaxy g)
	{
		map=g;
	}
}