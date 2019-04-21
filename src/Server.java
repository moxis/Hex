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
	boolean ai = false;

	public Server(String hostName, int portNumber, boolean ai) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.ai = ai;
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

			boolean done = false;
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
		NoHeuristicsAI hex = null;

		boolean done = false;
		boolean playerToPlay = false;

		try {
			if (in.readLine().equals("pass")) {
				hex = new NoHeuristicsAI();
				playerToPlay = false;
			} else {
				hex = new NoHeuristicsAI(Hex.DEFAULT_BOARD, -1);
				playerToPlay = true;
			}
			
			MonteCarlo mcts = new MonteCarlo(hex, true, true, 2000);

			String coord = "";
			int[] move = null;
			int x_value;
			int y_value;

			while (!done) {
				if (!playerToPlay) {
					if(!ai) {
						System.out.println("Please enter the x position to be placed at");
						x_value = Integer.parseInt(stdIn.readLine());

						System.out.println("Please enter the y position to be placed at");
						y_value = Integer.parseInt(stdIn.readLine());
						move = new int[] {x_value, y_value};
					} else {
						mcts.search(hex.getState());
						move = mcts.returnBestMove(hex.getState());

						x_value = move[0];
						y_value = move[1];
					}

					coord = ("(" + x_value + "," + y_value + ");");
					out.println(coord);
					hex.play(move);
					hex.printBoard();
					hex.connectWithNeighbors(move);
					if(hex.getWinner() != 0) {
						done = true;
					}

					playerToPlay = true;
				} else {
					System.out.println("Waiting for opponents move...");
					coord = in.readLine();
					System.out.println(coord);
					String[] axis = coord.replace(" ", "").split(",");
					
					x_value = Integer.parseInt(axis[0].replace("(", ""));
					y_value = Integer.parseInt(axis[1].replace(");", ""));
					move = new int[] {x_value, y_value};
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
		} finally {
			// System.out.println("you-win; bye");
			try {
				String finalMessage = in.readLine();
				if(finalMessage != null) {
					System.out.println(finalMessage);
				}
				clientSocket.close();
			} catch(IOException e) {}
			
		}

	}
}