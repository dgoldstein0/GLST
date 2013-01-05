import java.io.*;

public class TimeTester
{

	public static void main(String[] args)
	{
		try
		{
			BufferedReader theinput = new BufferedReader(new FileReader("input.txt"));
			String line;
			
			boolean[] shipseen = {false, false, false,false,false,false};
			Integer[] last_time = {null,null,null,null,null,null};
			Integer[] last_index = {null,null,null,null,null,null};
			
			int ship_id;
			int time;
			int index;
			
			while((line = theinput.readLine()) != null)
			{
				if(line.indexOf("saving time") != -1)
				{
					String[] splitup = line.split(" ");
					ship_id=Integer.parseInt(splitup[0]);
					time=Integer.parseInt(splitup[3]);
					index=Integer.parseInt(splitup[6]);
					if(shipseen[ship_id] && ((index-last_index[ship_id] != 1 && index-last_index[ship_id] != -49)||time-last_time[ship_id] != 20))
						System.out.println(line);
					shipseen[ship_id] = true;
					last_time[ship_id]=time;
					last_index[ship_id]=index;
				}
			}
			theinput.close();
		}
		catch(FileNotFoundException e){}
		catch(IOException ioe){}
	}
}