import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.HashSet;

public class Player
{
	String name;
	
	Object metal_lock = new Object();
	Object money_lock = new Object();
	double money;
	double metal;
	
	Color color;
	int id; //id is used to identify players.  These are assigned by the host of the game.
	boolean ready;
	HashSet<Ship> ships;
	
	//used for exploration.  specific to each player
	HashSet<GSystem> known_systems; //if you know a system, you know the stars in it.
	HashSet<Satellite> known_satellites;
	
	//this constructor prompts for the user to name the player himself
	public static Player createPlayer() throws CancelException
	{
		Player the_player=new Player();
		do
		{
			the_player.name=JOptionPane.showInputDialog("Please name your character.");
			if(!(the_player.name instanceof String))
				throw new CancelException();
		}
		while(the_player.name.equals(""));
		
		the_player.money=GalacticStrategyConstants.DEFAULT_MONEY;
		the_player.metal=GalacticStrategyConstants.DEFAULT_METAL;
		the_player.ready=false;
		the_player.ships=new HashSet<Ship>();
		the_player.known_systems = new HashSet<GSystem>();
		the_player.known_satellites = new HashSet<Satellite>();
		return the_player;
	}
	
	//used if the name of the player is already known to the program
	public Player(String nm)
	{
		name = nm;
		money=GalacticStrategyConstants.DEFAULT_MONEY;
		metal=GalacticStrategyConstants.DEFAULT_METAL;
		ships = new HashSet<Ship>();
		known_systems = new HashSet<GSystem>();
		known_satellites = new HashSet<Satellite>();
		ready=false;
	}
	
	public Color getColor()
	{
		if(!(color instanceof Color))
			color = GalacticStrategyConstants.DEFAULT_COLORS[getId()];
		return color;
	}
	
	public void setColor(Color c)
	{
		color=c;
	}
	
	//return true = successfully changed, return false = player doesn't have enough money.
	public boolean changeMoney(double m)
	{
		boolean ret=false;
		synchronized(money_lock){
			if(money+m >= 0)
			{
				money += m;
				ret=true;
			}
		}
		return ret;
	}
	
	//return true = successfully changed, return false = player doesn't have enough money.
	public boolean changeMetal(double m)
	{
		boolean ret=false;
		synchronized(metal_lock){
			if(metal + m >= 0)
			{
				metal += m;
				ret=true;
			}
		}
		return ret;
	}
	
	//methods necessary for saving/loading
	public Player(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public double getMoney(){return money;}
	public void setMoney(double m){money=m;}
	public double getMetal(){return metal;}
	public void setMetal(double m){metal=m;}
	public int getId(){return id;}
	public void setId(int x){id=x;}
	public boolean getReady(){return ready;}
	public void setReady(boolean r){ready=r;}
}