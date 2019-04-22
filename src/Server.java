import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.sun.security.ntlm.Client;

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

	/**
	 * Constructor for the Client Class
	 * 
	 * @param hostName   String that represents the host to be used in the
	 *                   connection
	 * @param portNumber Integer representing the port to be connected to
	 * @param ai         Boolean representing whether it will be ai or a local
	 *                   player playing
	 */
	public Server(String hostName, int portNumber, boolean ai) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.ai = ai;
	}

	/**
	 * Connects user to client and starts game.
	 */
	public void communicate() {
		try {
			connect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Starts the connection and allows a client to connect, transfer the correct
	 * protocol between the client and server and then starrt a game of Hex
	 */
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
				try {
					if (in.readLine().equals("hello")) {
						out.println("hello");
						if (in.readLine().equals("newgame")) {
							out.println("ready");
							play(out, in, stdIn);
						}
					}
				} catch (NullPointerException e) {}
			}

		} catch (SocketException e) {
			System.out.println(e.getMessage());
			out.println("reject");
		} catch (IOException e) {
			System.out.println("There was in issue while reading in from the client");
			System.exit(0);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("There was an issue when closing the socket");
				System.exit(0);
			}
		}
	}

	/**
	 * The method used to play against a connected client in a game of hex.
	 * 
	 * @param out   PrintWriter used to send commands to the server
	 * @param in    BufferedReader used to read in commands from the server
	 * @param stdIn BufferedReader used to read in entry from the terminal keyboard.
	 * @throws NullPointerException Must be thrown in case an unexpected
	 *                              disconnection occurs from the server.
	 */
	public void play(PrintWriter out, BufferedReader in, BufferedReader stdIn) throws NullPointerException {
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
					if (!ai) {
						while(true) {
							try {
								System.out.println("Please enter the x position to be placed at");
								String x_string = stdIn.readLine();
								if (x_string.equals("bye")) {
									out.println("bye");
									System.exit(0);
								}
								x_value = Integer.parseInt(x_string);
	
								System.out.println("Please enter the y position to be placed at");
								String y_string = stdIn.readLine();
								if (y_string.equals("bye")) {
									out.println("bye");
									System.exit(0);
								}
								y_value = Integer.parseInt(y_string);
								
								if(x_value < 0 || x_value >= Hex.BOARD_SIZE || y_value < 0 || y_value >= Hex.BOARD_SIZE) {
									throw new NumberFormatException();
								}

								break;
							} catch(NumberFormatException e) {
								System.out.println("Invalid coordinates.. Try again");
							}
						}

						
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

						playerToPlay = true;
					} else {
						System.out.println("Invalid move! Please enter again...");
					}
				} else {
					System.out.println("Waiting for opponents move...");
					coord = in.readLine();
					System.out.println(coord);
					if (coord.equals("bye")) {
						clientSocket.close();
						System.exit(0);
					}
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

						playerToPlay = false;
					} else {
						System.out.println("Received invalid move! Quitting...");
						clientSocket.close();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in coords");
		} catch (NullPointerException e) {
			System.out.println("Unexpected disconnection from the client");
			System.exit(0);
		} finally {
			// System.out.println("you-win; bye");
			try {
				String finalMessage = in.readLine();
				if (finalMessage != null) {
					System.out.println(finalMessage);
				}
				clientSocket.close();
				System.out.println("Game finished! Restarting server...");
				serverSocket.close();
				this.communicate();
			} catch (IOException e) {
			}

		}

	}
}