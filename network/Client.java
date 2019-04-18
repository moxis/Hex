package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Client {
	String hostName;
	int portNumber;
	Socket clientSocket;
	BufferedReader in;
	BufferedReader stdIn;
	PrintWriter out;

    
    public Client(String hostName, int portNumber){
    	this.hostName = hostName;
    	this.portNumber = portNumber;
    }

  //Connects user to servers and starts game.
	public void communicate(){
		try {
			connect();
		}		
		catch(NullPointerException e){
			System.out.println("\nLost connection with the server.");
			try {
				clientSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		catch(Exception e){ 
			e.printStackTrace();
		}
		finally{
		}
	}
	
	public void connect() throws NullPointerException{
		try {
			stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			clientSocket = new Socket(hostName, portNumber);
		    out = new PrintWriter(clientSocket.getOutputStream(), true);					//Sets up output
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));	//Sets up input
			
			if (in.readLine().equals("hello")){
				System.out.println("Server says hello");
				out.println("hello");
			}
		
		} catch (ConnectException e){
			System.out.println("There was no server availible. Press enter to try again");
			try {
				stdIn.readLine();
				connect();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}