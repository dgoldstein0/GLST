import java.io.IOException;

public class TestGameClient
{
	public static void main(String[] args)
	{
		GameControl GC = new GameControl();
		GC.joinAsClient();
		
		try
		{
			GC.downloadAndLoadMap(true);
			System.out.println("map loaded");
		}
		catch(IOException e)
		{
			System.out.println("Map loading failed.  Ending connection");
			GC.endConnection();
		}
		
		GC.startGame();
	}
}