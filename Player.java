import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public strictfp class Player implements RelaxedSaveable<Player>
{
	String name;
	
	Object metal_lock = new Object();
	Object money_lock = new Object();
	
	/**any function updating both metal and money should grab metal_lock before money_lock
	 * @GaurdedBy money_lock*/
	private double money;
	
	/**any function updating both metal and money should grab metal_lock before money_lock
	 * @GaurdedBy metal_lock*/
	private double metal;
	
	long last_time;
	List<Saveable<?>> resource_users;
	PlayerDataSaverControl data_control;
	
	Color color;
	int id; //id is used to identify players.  These are assigned by the host of the game.
	boolean ready;
	boolean hosting; //true if running as server, false if running as client
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
		hosting = is_host;
		data_control = new PlayerDataSaverControl(this);
		setDefaultValues();
	}
	
	private void setDefaultValues()
	{
		money=GalacticStrategyConstants.DEFAULT_MONEY;
		metal=GalacticStrategyConstants.DEFAULT_METAL;
		ships_in_transit = new ArrayList<Ship>();
		known_systems = new ArrayList<GSystem>();
		known_satellites = new ArrayList<Satellite<?>>();
		ready=false;
		resource_users = new ArrayList<Saveable<?>>();
		last_time=0;
	}
	
	public Color getColor()
	{
		if(color == null)
			color = GalacticStrategyConstants.DEFAULT_COLORS[getId()];
		return color;
	}
	
	public void setColor(Color c)
	{
		color=c;
	}
	
	//return true = successfully changed, return false = player doesn't have enough money.
	public boolean changeMoney(double m, long t, Saveable<?> requester)
	{
		boolean ret=false;
		synchronized(money_lock){
			if(money+m >= 0)
			{
				money += m;
				ret=true;
			}
			
			//if request fails, a roll-back might cause it to succeed, so still save the requester
			if(t != last_time)
					resource_users.clear();
			last_time=t;
			resource_users.add(requester);
			data_control.saveData();
		}
		return ret;
	}
	
	//return true = successfully changed, return false = player doesn't have enough money.
	public boolean changeMetal(double m, long t, Saveable<?> requester)
	{
		boolean ret=false;
		synchronized(metal_lock){
			if(metal + m >= 0)
			{
				metal += m;
				ret=true;
			}
			
			//if request fails, a roll-back might cause it to succeed, so still save the requester
			if(t != last_time)
				resource_users.clear();
			last_time=t;
			resource_users.add(requester);
			data_control.saveData();
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
	public double getMoney(){synchronized(money_lock){return money;}}
	public void setMoney(double m){money=m;}
	public double getMetal(){synchronized(metal_lock){return metal;}}
	public void setMetal(double m){metal=m;}
	public int getId(){return id;}
	public void setId(int x){id=x;}
	public boolean getReady(){return ready;}
	public void setReady(boolean r){ready=r;}
	public ArrayList<Ship> getShips_in_transit(){return ships_in_transit;}
	public void setShips_in_transit(ArrayList<Ship> s){ships_in_transit=s;}

	@Override
	public PlayerDataSaverControl getDataControl() {
		return data_control;
	}

	@Override
	public long getTime() {
		return last_time;
	}
	
	@Override
	public void handleDataNotSaved(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTime(long t) {last_time=t;}
	
	public List<Saveable<?>> getResource_users(){return resource_users;}
	public void setResource_users(List<Saveable<?>> l){resource_users=l;}
}