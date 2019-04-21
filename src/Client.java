
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import ai.*;
import game.*;

public class Client {
	String hostName;
	int portNumber;
	Socket clientSocket;
	BufferedReader in;
	BufferedReader stdIn;
	PrintWriter out;
	boolean ai = false;

	public Client(String hostName, int portNumber, boolean ai) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.ai = ai;
	}

	// Connects user to servers and starts game.
	public void communicate() {
		try {
			connect();
		} catch (NullPointerException e) {
			System.out.println("\nLost connection with the server.");
			try {
				clientSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {}
	}

	public void connect() throws NullPointerException {
		try {
			stdIn = new BufferedReader(new InputStreamReader(System.in));

			clientSocket = new Socket(hostName, portNumber);
			out = new PrintWriter(clientSocket.getOutputStream(), true); // Sets up output
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Sets up input

			out.println("hello");
			if (in.readLine().equals("hello")) {
				System.out.println("hello");
				out.println("newgame");
				if (in.readLine().equals("ready")) {
					System.out.println("ready");
					play(out, in, stdIn);
				}
			}

		} catch (ConnectException e) {
			System.out.println("There was no server available. Press enter to try again");
			try {
				stdIn.readLine();
				connect();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void play(PrintWriter out, BufferedReader in, BufferedReader stdIn) {
		NoHeuristicsAI hex = null;

		String str = "";
		boolean done = false;

		System.out.println("Type 1 to play first, type 2 to pass");
		
		try {
			str = stdIn.readLine();
			while (!(str.equals("1") || str.equals("2"))) {
				System.out.println("Try Again");

				str = stdIn.readLine();
			}
		} catch (NullPointerException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		// true represents client, false server
		boolean playerToPlay = false;
		if (str.equals("1")) {
			hex = new NoHeuristicsAI(Hex.DEFAULT_BOARD, -1);
			playerToPlay = true;
			out.println("");
		}
		else {
			hex = new NoHeuristicsAI();
			out.println("pass");
		}
		
		MonteCarlo mcts = new MonteCarlo(hex, true, true, 2000);
		try {
			String coord = "";
			int[] move = null;
			int x_value;
			int y_value;
			while (!done) {
				if (playerToPlay) {
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

					playerToPlay = false;
				}
				else {
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

					playerToPlay = true;
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
