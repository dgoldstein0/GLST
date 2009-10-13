import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Player
{
	static long DEFAULT_MONEY=100;
	
	String name;
	long money;
	long metal;
	
	public static Player createPlayer()
	{
		Player the_player=new Player();
		do
		{
			the_player.name=JOptionPane.showInputDialog("Please name your character.");
		}
		while(!(the_player.name instanceof String) || the_player.name.equals(""));
		
		the_player.money=DEFAULT_MONEY;
		return the_player;
	}
	
	//methods necessary for saving/loading
	public Player(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public long getMoney(){return money;}
	public void setMoney(long m){money=m;}
	public long getMetal(){return metal;}
	public void setMetal(long m){metal=m;}
	
}