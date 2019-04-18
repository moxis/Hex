package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class Server {
	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	String hostName;
	int portNumber;
	BufferedReader in;
	BufferedReader stdIn;
	PrintWriter out;

	
	public Server(String hostName, int portNumber){
		this.hostName=hostName;
		this.portNumber=portNumber;
	}
	
	//Connects user to client and starts game.
	public void communicate(){
		try {
			connect();
		    }
		catch(Exception e){ 
			e.printStackTrace();
		}
		
	}

	public void connect() {
		try { 
		    serverSocket = new ServerSocket(portNumber);
		    System.out.println("Waiting for client...");
		    clientSocket = serverSocket.accept();
		    
		    System.out.println("Client found.");
		    out = new PrintWriter(clientSocket.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    stdIn = new BufferedReader(new InputStreamReader(System.in));
		    
		    //Starts communication with client
		    out.println("hello");
		    //Makes sure client is using the same protocols
		    if(in.readLine().equals("hello")){
		    	System.out.println("Client says hello");
		    }
		    
		}catch(SocketException e){
			System.out.println(e.getMessage());

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}