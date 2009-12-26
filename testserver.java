import java.io.*;
import java.net.*;




public class testserver {
	
	public static void main(String args[]) throws IOException  
    {  
		
		ServerSocket serverSocket = null;
		try {
		    serverSocket = new ServerSocket(7008);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: 7008");
		    System.exit(-1);
		}

		Socket clientSocket = null;
		try {
		    clientSocket = serverSocket.accept();
		} catch (IOException e) {
		    System.out.println("Accept failed: 7008");
		    System.exit(-1);
		}
		
		PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
                      clientSocket.getInputStream()));
		
        BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));
                
        String userInput;

          	    while ((userInput = stdIn.readLine()) != null) {
             	    out.println(userInput);
             	    System.out.println("echo: " + in.readLine());
          	    }
		
		out.close();
		in.close();
  	    stdIn.close();
		serverSocket.close();

    }

}
