import java.io.IOException;

public class TestGameClient
{
	public static void main(String[] args)
	{
		GameControl GC = new GameControl();
		GC.joinAsClient();
		GC.startGame();
	}
}