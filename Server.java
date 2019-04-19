
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
			
			Boolean done = false;
			while (!done) {
				//System.out.println("HELP");
				out.println("HEllO");
				if(in.readLine().equals("hello")){
					out.println("hello");
					if(in.readLine().equals("New-game")) {
						out.println("ready");
						//Insert code to play the game (couldn't see how to turn the code from AI play into client vs server play)

						//Insert code to choose if AI or player is playings
					}
				}
			}
			
		    
		}catch(SocketException e){
			System.out.println(e.getMessage());
			out.println("reject");

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