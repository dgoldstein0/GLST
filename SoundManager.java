import  sun.audio.*;    //import the sun.audio package
import  java.io.*;

public class SoundManager
{
	public static void playSound(String filename)
	{
		try{
			// Open an input stream  to the audio file.
			InputStream in = new FileInputStream(filename);
			
			// Create an AudioStream object from the input stream.
			AudioStream as = new AudioStream(in);
			
			// Use the static class member "player" from class AudioPlayer to play clip.
			AudioPlayer.player.start(as);
			
			// Similarly, to stop the audio.
			//AudioPlayer.player.stop(as); 
		} catch (IOException ioe){System.out.println("sound file not found!");}
	}
}