import java.io.*;

public class Tester
{

	public static void main(String[] args)
	{
		int a, b;
		try
		{
			BufferedReader theinput = new BufferedReader(new FileReader("input.txt"));
			String line;
			while((line = theinput.readLine()) instanceof String)
			{
				String[] splitup = line.split(" ");
				a=Integer.parseInt(splitup[2]);
				b=Integer.parseInt(splitup[6]);
				if(a != b)
					System.out.println("mismatch " + splitup[2] + " " + splitup[6]);
			}
			theinput.close();
		}
		catch(FileNotFoundException e){}
		catch(IOException ioe){}
	}
}