
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

	public Client(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
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
		} finally {
		}
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
				out.println("new-game");
				if (in.readLine().equals("ready")) {
					System.out.println("ready");
					play(out, in, stdIn);
				}
			}

		} catch (ConnectException e) {
			System.out.println("There was no server availible. Press enter to try again");
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
		boolean done = false;

		System.out.println("Type 1 to play first, type 2 to pass");
		String str = "";
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
			playerToPlay = true;
		}
		else {
			out.println("pass");
		}
		try {
			while (!done) {
				if (playerToPlay) {
					System.out.println("Please enter the x position to be placed at");
					String x_value = stdIn.readLine();

					System.out.println("Please enter the y position to be placed at");
					String y_value = stdIn.readLine();

					String coord = ("(" + x_value + ", " + y_value + ")");

					out.println(coord);

					playerToPlay = false;
				}
				else {
					System.out.println("Waiting for opponents move...");
					String coord = in.readLine();
					System.out.println(coord);

					playerToPlay = true;
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in coords");
		}

	}
}
