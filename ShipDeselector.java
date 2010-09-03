public class ShipDeselector implements Runnable
{
	final Ship the_ship;
	
	public ShipDeselector(Ship s)
	{
		the_ship = s;
	}
	
	public void run()
	{
		if(the_ship == GameInterface.GC.GI.ShipPanel.the_ship)
		{
			GameInterface.GC.GI.displayNoPanel();
		}
		GameInterface.GC.GI.selected_in_sys.remove(the_ship);	
	}
}
