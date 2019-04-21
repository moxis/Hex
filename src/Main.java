import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import game.*;
import ai.*;


public class Main {
    public static void main(String[] args) {
        Hex.initializeWinConditions();
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Would you like local play (1) or server play (2)?");
        String entry = "";

        try {
            entry = stdIn.readLine();
            while(!(entry.equals("1")||entry.equals("2"))){
                System.out.println("Try Again");

                entry = stdIn.readLine();
            }
        } 
        catch (NullPointerException e){} 
        catch (IOException e) {
            e.printStackTrace();
        }   

        if (entry.equals("1")) {
            // AI vs AI or Human vs AI or Human vs Human
        }
        else {
            String hostName = "";
            int portNumber = 9128;

            InetAddress IP;	//Used to find user's IP address
            try {
                IP = InetAddress.getLocalHost();
                //Lets the user know what their IP address is to tell the opponent if they are server
                System.out.println("IP of my system is := "+IP.getHostAddress());
                hostName = IP.getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            //Lets user decide whether they are acting as a server or a client
            System.out.println("Type 1 for a server. Type 2 for a client");
            String str = "";
                try {
                    str = stdIn.readLine();
                    while(!(str.equals("1")||str.equals("2"))){
                        System.out.println("Try Again");

                    str = stdIn.readLine();
                    }
                } catch (NullPointerException e){
                } catch (IOException e) {
                    e.printStackTrace();
            }
            //Sets up server
            if(str.equals("1")){
                Server serv = new Server(hostName, portNumber);
                serv.communicate();
            }
            //Sets up client
            else{
                //Allows user to type in the IP address of the computer they want to play against.
                System.out.println("Please type in the IP Address that you want to compete against.");
                try {
                    String ipAddress = getIPAddress();
                    Client client = new Client(ipAddress, portNumber);
                    client.communicate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
	    }
    }
    
    public static String getIPAddress() throws IOException{
	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String ipAddress = stdIn.readLine();
		String[] splitIP = ipAddress.split("\\.");
		boolean pass = true;
		//Makes sure its in the correct format
		if(splitIP.length == 4){
			for(int i = 0; i < 4; i++){
				//Makes sure that they are all integers
			}
		} else pass = false;
		if(pass == false){
			//Gets the user to try again if its not a valid address
			System.out.println("Not a valid IP Address. Try Again.");
			ipAddress = getIPAddress();
		}
		return ipAddress;
    }
}