import java.io.File;
import java.io.FileNotFoundException;

public class TestGameServer
{
	public static void main(String[] args)
	{
		GameControl GC = new GameControl();
		GC.host();
		try
		{
			GC.serverThread.join();
		}
		catch(InterruptedException IE){return;}
		
		File cur_file = new File("C:\\Users\\David\\Desktop\\zoom_test.xml");
		try
		{
			GC.loadMap(cur_file);
			GC.sendMap();
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("File not found.  Ending Connection...");
			GC.endConnection();
			return;
		}
		
		GC.startGame();
	}
}