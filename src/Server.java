import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import ai.*;
import game.*;

public class Server {
	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	String hostName;
	int portNumber;
	BufferedReader in;
	BufferedReader stdIn;
	PrintWriter out;

	public Server(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
	}

	// Connects user to client and starts game.
	public void communicate() {
		try {
			connect();
		} catch (Exception e) {
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
				if (in.readLine().equals("hello")) {
					out.println("hello");
					if (in.readLine().equals("new-game")) {
						out.println("ready");
						play(out, in, stdIn);
					}
				}
			}

		} catch (SocketException e) {
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

	public void play(PrintWriter out, BufferedReader in, BufferedReader stdIn) {
		NoHeuristicsAIwithSaveBridgeSimulation hex = new NoHeuristicsAIwithSaveBridgeSimulation();
		boolean done = false;
		boolean playerToPlay = false;

		try {
			if (in.readLine().equals("pass")) {
				playerToPlay = false;
			} else {
				playerToPlay = true;
			}
			
			while (!done) {
				if (!playerToPlay) {
					System.out.println("Please enter the x position to be placed at");
					int x_value = Integer.parseInt(stdIn.readLine());

					System.out.println("Please enter the y position to be placed at");
					int y_value = Integer.parseInt(stdIn.readLine());

					String coord = ("(" + x_value + "," + y_value + ");");

					int[] move = new int[] {x_value, y_value};

					out.println(coord);
					hex.play(move);
					hex.printBoard();
					hex.connectWithNeighbors(move);
					if(hex.getWinner() != 0) {
						done = true;
						out.println("you-win; bye");
					}

					playerToPlay = true;
				} else {
					System.out.println("Waiting for opponents move...");
					String coord = in.readLine();
					System.out.println(coord);
					String[] axis = coord.split(",");
					
					int x_value = Integer.parseInt(axis[0].replace("(", ""));
					int y_value = Integer.parseInt(axis[1].replace(");", ""));
					int[] move = new int[] {x_value, y_value};
					hex.play(move);
					hex.printBoard();
					hex.connectWithNeighbors(move);
					if(hex.getWinner() != 0) {
						done = true;
						out.println("you-win; bye");
					}

 					playerToPlay = false;
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in coords");
		}

	}
}