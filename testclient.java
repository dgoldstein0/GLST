  import javax.swing.JOptionPane;  
  import java.io.*;
  import java.net.*;
  
  public class testclient   {  
   
        public static void main(String args[]) throws IOException  
        {  
        	  Socket echoSocket = null;
              PrintWriter out = null;
              BufferedReader in = null;

              String ip_in_string;                                            
              ip_in_string=JOptionPane.showInputDialog("Enter IP address");
              byte[] ip_in_byte=new byte[4];
              String[] ip=ip_in_string.split(".");              
              for (int i=0; i<=3; i++)
            	  ip_in_byte[i]=(byte) Integer.parseInt(ip[i]);      
              InetAddress ipaddress=InetAddress.getByAddress(ip_in_byte);
              
              try {
                  echoSocket = new Socket(ipaddress, 7007);
                  out = new PrintWriter(echoSocket.getOutputStream(), true);
                  in = new BufferedReader(new InputStreamReader(
                                              echoSocket.getInputStream()));
                  
              } catch (UnknownHostException e) {
                  System.err.println("Don't know about host");
                  System.exit(1);
              } catch (IOException e) {
                  System.err.println("Couldn't get I/O for "
                                     + "the connection to the host");
              }

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
      	    echoSocket.close();
        }  
  }   