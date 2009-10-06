import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Player
{
	String name;
	long money;
	
	public static Player createPlayer()
	{
		Player the_player=new Player();
		do
		{
			the_player.name=JOptionPane.showInputDialog("Please name your character.");
		}
		while(!(the_player.name instanceof String) || the_player.name.equals(""));
		
		the_player.money=2000;
		return the_player;
	}
	
	//methods necessary for saving/loading
	public Player(){}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public long getMoney(){return money;}
	public void setMoney(long m){money=m;}
	
}