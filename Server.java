
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
		boolean done = false;
		boolean playerToPlay = false;

		try {
			if (in.readLine().equals("pass")) {
				playerToPlay = false;
			}
			while (!done) {
				if (!playerToPlay) {
					System.out.println("Please enter the x position to be placed at");
					String x_value = stdIn.readLine();

					System.out.println("Please enter the y position to be placed at");
					String y_value = stdIn.readLine();

					String coord = ("(" + x_value + ", " + y_value + ")");

					out.println(coord);

					playerToPlay = true;
				} else {
					System.out.println("Waiting for opponents move...");
					String coord = in.readLine();
					System.out.println(coord);

					playerToPlay = false;
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while reading in coords");
		}

	}
}