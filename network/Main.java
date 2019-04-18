package network;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Main {
	static String hostName;
	static int portNumber = 9898;
	public static int player;
	public static void main(String[] args) {
		
		InetAddress IP;	//Used to find user's IP address
		try {
			IP = InetAddress.getLocalHost();
			//Lets the user know what their IP address is to tell the opponent if they are server
			System.out.println("IP of my system is := "+IP.getHostAddress());
			hostName = IP.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
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
	
	//Gets an IP address of the user
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