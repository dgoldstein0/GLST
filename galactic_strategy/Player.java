package galactic_strategy;
import galactic_strategy.game_objects.GSystem;
import galactic_strategy.game_objects.Satellite;
import galactic_strategy.game_objects.Ship;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Saveable;

import javax.swing.JOptionPane;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public strictfp class Player implements Saveable<Player>
{
	String name;
	
	private Object metal_lock = new Object();
	private Object money_lock = new Object();
	
	/**
	 * any function updating both metal and money should grab metal_lock before money_lock
	 * @GaurdedBy money_lock
	 */
	private double money;
	
	/**
	 * any function updating both metal and money should grab metal_lock before money_lock
	 * @GaurdedBy metal_lock
	 */
	private double metal;
	
	public DataSaverControl<Player> data_control;
	
	Color color;
	int id; //id is used to identify players.  These are assigned by the host of the game.
	
	/**
	 * Used to give logical timestamps to orders at creation time.
	 */
	private AtomicInteger next_order_number;
	
	boolean ready;
	private boolean hosting; //true if running as server, false if running as client
	ArrayList<Ship> ships_in_transit;
	
	//used for exploration.  specific to each player
	ArrayList<GSystem> known_systems; //if you know a system, you know the stars in it.
	ArrayList<Satellite<?>> known_satellites;
	
	//this constructor prompts for the user to name the player himself
	public static Player createPlayer(boolean hosting) throws CancelException
	{
		String name;
		do
		{
			name=JOptionPane.showInputDialog("Please choose a Screen Name.");
			if(name == null)
				throw new CancelException();
		}
		while(name.equals(""));
		
		return new Player(name, hosting);
	}
	
	//used if the name of the player is already known to the program
	public Player(String nm, boolean is_host)
	{
		name = nm;
		setHosting(is_host);
		setDefaultValues();
	}
	
	private void setDefaultValues()
	{
		data_control = new DataSaverControl<Player>(this);
		next_order_number = new AtomicInteger(0);
		money=Constants.DEFAULT_MONEY;
		metal=Constants.DEFAULT_METAL;
		ships_in_transit = new ArrayList<Ship>();
		known_systems = new ArrayList<GSystem>();
		known_satellites = new ArrayList<Satellite<?>>();
		ready=false;
	}
	
	public Color getColor()
	{
		if(color == null)
			color = Constants.DEFAULT_COLORS[getId()];
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
		synchronized(getMoney_lock()){
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
		synchronized(getMetal_lock()){
			if(metal + m >= 0)
			{
				metal += m;
				ret=true;
			}
		}
		return ret;
	}
	
	//methods necessary for saving/loading
	public Player()
	{
		name="";
		setDefaultValues();
	}

	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public double getMoney(){synchronized(getMoney_lock()){return money;}}
	public void setMoney(double m){money=m;}
	public double getMetal(){synchronized(getMetal_lock()){return metal;}}
	public void setMetal(double m){metal=m;}
	public int getId(){return id;}
	public void setId(int x){id=x;}
	public boolean getReady(){return ready;}
	public void setReady(boolean r){ready=r;}
	public ArrayList<Ship> getShips_in_transit(){return ships_in_transit;}
	public void setShips_in_transit(ArrayList<Ship> s){ships_in_transit=s;}
	
	@Override
	public DataSaverControl<Player> getDataControl() {
		return data_control;
	}

	public int getNextOrderNumber() {
		return next_order_number.getAndIncrement();
	}

	public void update(long time)
	{	
		Iterator<Ship> ship_it = ships_in_transit.iterator();
		Ship s;
		
		while(ship_it.hasNext())
		{
			s=ship_it.next();
			s.moveDuringWarp(time, ship_it); //the iterator is passed so that moveDuringWarp can remove the ship from the iteration, and by doing so from ships_in_transit
		}
	}

	public boolean isHosting() {
		return hosting;
	}

	public void setHosting(boolean hosting) {
		this.hosting = hosting;
	}

	public Object getMetal_lock() {
		return metal_lock;
	}

	public Object getMoney_lock() {
		return money_lock;
	}
}