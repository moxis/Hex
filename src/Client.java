
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

/**
 * Class to handle the server operations for the Client side of the networking.
 */
public class Client {
	String hostName;
	int portNumber;
	Socket clientSocket;
	BufferedReader in;
	BufferedReader stdIn;
	PrintWriter out;
	boolean ai = false;

	/**
	 * Constructor for the Client Class
	 * 
	 * @param hostName   String that represents the host to be used in the
	 *                   connection
	 * @param portNumber Integer representing the port to be connected to
	 * @param ai         Boolean representing whether it will be ai or a local
	 *                   player playing
	 */
	public Client(String hostName, int portNumber, boolean ai) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.ai = ai;
	}

	/**
	 * Used to connect user to the server, and perform error handling if that is
	 * unsuccessful
	 */
	public void communicate() {
		try {
			connect();
		} catch (NullPointerException e) {
			System.out.println("\nLost connection with the server.");
			try {
				clientSocket.close();
			} catch (IOException e1) {
				System.out
						.println("Something went wrong while reading in the connection, please reload and try again.");
				System.exit(0);
			}
		} catch (Exception e) {
			System.out.println("Something went wrong while connecting, please reload and try again.");
			System.exit(0);
		} finally {
		}
	}

	/**
	 * Connects the client to a designated server running Hex
	 * 
	 * @throws NullPointerException Used if the connection to the server is
	 *                              interrupted
	 */
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
				System.out
						.println("Something went wrong while reading in the connection, please reload and try again.");
				System.exit(0);
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in from the server, please reload and try again.");
			System.exit(0);
		}
	}

	/**
	 * The method used to play against a connected server in a game of hex.
	 * 
	 * @param out   PrintWriter used to send commands to the server
	 * @param in    BufferedReader used to read in commands from the server
	 * @param stdIn BufferedReader used to read in entry from the terminal keyboard.
	 * @throws NullPointerException Must be thrown in case an unexpected
	 *                              disconnection occurs from the server.
	 */
	public void play(PrintWriter out, BufferedReader in, BufferedReader stdIn) throws NullPointerException {
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
		} else {
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
					if (!ai) {
						System.out.println("Please enter the x position to be placed at");
						x_value = Integer.parseInt(stdIn.readLine());

						System.out.println("Please enter the y position to be placed at");
						y_value = Integer.parseInt(stdIn.readLine());
						move = new int[] { x_value, y_value };
					} else {
						mcts.search(hex.getState());
						move = mcts.returnBestMove(hex.getState());

						x_value = move[0];
						y_value = move[1];
					}

					coord = ("(" + x_value + "," + y_value + ");");
					if(hex.getState()[move[0]][move[1]] == 0) {
						out.println(coord);
						hex.play(move);
						hex.printBoard();
						hex.connectWithNeighbors(move);
						if (hex.getWinner() != 0) {
							done = true;
						}

						playerToPlay = false;
					} else {
						System.out.println("Invalid move! Please enter again...");
					}
				} else {
					System.out.println("Waiting for opponents move...");
					coord = in.readLine();
					System.out.println(coord);
					String[] axis = coord.replace(" ", "").split(",");

					x_value = Integer.parseInt(axis[0].replace("(", ""));
					y_value = Integer.parseInt(axis[1].replace(");", ""));
					move = new int[] { x_value, y_value };

					if(hex.getState()[move[0]][move[1]] == 0) {
						hex.play(move);
						hex.printBoard();
						hex.connectWithNeighbors(move);
						if (hex.getWinner() != 0) {
							done = true;
							out.println("you-win; bye");
						}

						playerToPlay = true;
					} else {
						System.out.println("Received invalid move! Quitting...");
						clientSocket.close();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in coords or reading in messages from the server.");
			System.exit(0);
		} catch (NullPointerException e) {
			System.out.println("Unexpected disconnection from the server");
			System.exit(0);
		} finally {
			try {
				String finalMessage = in.readLine();
				if (finalMessage != null) {
					System.out.println(finalMessage);
				}
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("Something went wrong while reading in entry from the server.");
				System.exit(0);
			}
		}
	}
}
