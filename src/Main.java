import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import game.*;
import ai.*;

public class Main {
    /**
     * Performs the initialisation of the program, allowing the user to choose
     * things such as if it is an AI playing, if it is local or server play, and
     * whehter they are a server or a client.
     * 
     * @param args Array of Strings that should not need to be read in by the
     *             program at any point.
     */
    public static void main(String[] args) {
        Hex.initializeWinConditions();
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        boolean ai = false;
        System.out.println("Would you like local play (1) or server play (2)?");
        String entry = "";
        String str = "";

        try {
            entry = stdIn.readLine();
            while (!(entry.equals("1") || entry.equals("2"))) {
                System.out.println("Try Again");

                entry = stdIn.readLine();
            }
        } catch (NullPointerException e) {
            System.out.println("There was no entry, please reload and try again");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Something went wrong while reading in from the terminal, pease reload and try again");
            System.exit(0);
        }

        if (entry.equals("1")) {
            // AI vs AI or Human vs AI or Human vs Human
            System.out.println("Please enter 1 for AI vs AI, 2 for AI vs Human, or 3 for Human vs Human");
            String choice = "";
            try {
                choice = stdIn.readLine();
                while (!(choice.equals("1") || choice.equals("2") || choice.equals("3"))) {
                    System.out.println("Try Again");

                    choice = stdIn.readLine();
                }
            } catch (NullPointerException e) {
                System.out.println("There was no entry, please reload and try again");
                System.exit(0);
            } catch (IOException e) {
                System.out
                        .println("Something went wrong while reading in from the terminal, pease reload and try again");
                System.exit(0);
            }

            if (choice.equals("1")) {
                play(true, true);
            } else if (choice.equals("2")) {
                play(false, true);
            } else if (choice.equals("3")) {
                play(false, false);
            }
        } else {
            String hostName = "";
            int portNumber = 5000;

            InetAddress IP; // Used to find user's IP address
            try {
                IP = InetAddress.getLocalHost();
                // Lets the user know what their IP address is to tell the opponent if they are
                // server
                System.out.println("IP of my system is := " + IP.getHostAddress());
                hostName = IP.getHostAddress();
            } catch (UnknownHostException e) {
                System.out.println("There was an issue when trying to find the local IP");
                System.exit(0);
            }

            System.out.println("Type 1 to play manually, type 2 for AI player");
            try {
                while (str.equals("")) {
                    str = stdIn.readLine();
                    while (!(str.equals("1") || str.equals("2"))) {
                        System.out.println("Try Again");
                        str = stdIn.readLine();
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("There was no entry, please reload and try again");
                System.exit(0);
            } catch (IOException e) {
                System.out
                        .println("Something went wrong while reading in from the terminal, pease reload and try again");
                System.exit(0);
            }
            if (str.equals("2")) {
                ai = true;
            }

            // Lets user decide whether they are acting as a server or a client
            System.out.println("Type 1 for a server. Type 2 for a client");
            try {
                str = stdIn.readLine();
                while (!(str.equals("1") || str.equals("2"))) {
                    System.out.println("Try Again");

                    str = stdIn.readLine();
                }
            } catch (NullPointerException e) {
                System.out.println("There was no entry, please reload and try again");
                System.exit(0);
            } catch (IOException e) {
                System.out
                        .println("Something went wrong while reading in from the terminal, pease reload and try again");
                System.exit(0);
            }
            // Sets up server
            if (str.equals("1")) {
                Server serv = new Server(hostName, portNumber, ai);
                serv.communicate();
            }
            // Sets up client
            else {
                // Allows user to type in the IP address of the computer they want to play
                // against.
                System.out.println("Please type in the IP Address that you want to compete against.");
                try {
                    String ipAddress = getIPAddress();
                    Client client = new Client(ipAddress, portNumber, ai);
                    client.communicate();
                } catch (IOException e) {
                    System.out
                            .println("There was an issue while reading in the IP address, please reload and try again");
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Method used to have the Client enter in a correctly formatted IP Address for
     * the Server to be connected to.
     * 
     * @return String representing the IP address to be connected to.
     * @throws IOException Must be thrown in case of any errors while trying to read
     *                     in from the terminal.
     */
    public static String getIPAddress() throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String ipAddress = stdIn.readLine();
        String[] splitIP = ipAddress.split("\\.");
        boolean pass = true;
        // Makes sure its in the correct format
        if (splitIP.length == 4) {
            for (int i = 0; i < 4; i++) {
                // Makes sure that they are all integers
            }
        } else
            pass = false;
        if (pass == false) {
            // Gets the user to try again if its not a valid address
            System.out.println("Not a valid IP Address. Try Again.");
            ipAddress = getIPAddress();
        }
        return ipAddress;
    }

    public static void play(boolean AI_1, boolean AI_2) {
        NoHeuristicsAI hex1 = null;
        NoHeuristicsAI hex2 = null;

        boolean done = false;
        boolean playerToPlay = true;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        try {
            if (AI_1) {
				hex1 = new NoHeuristicsAI();
				playerToPlay = false;
            }
            if (AI_2) {
				hex2 = new NoHeuristicsAI(Hex.DEFAULT_BOARD, -1);
				playerToPlay = true;
			}

            MonteCarlo mcts1 = new MonteCarlo(hex1, true, true, 2000);

            MonteCarlo mcts2 = new MonteCarlo(hex2, true, true, 2000);

            String coord = "";
            int[] move = null;
            int x_value;
            int y_value;

            while (!done) {
                if (!playerToPlay) {
                    if (!AI_1) {
                        System.out.println("Please enter the x position to be placed at");
                        x_value = Integer.parseInt(stdIn.readLine());

                        System.out.println("Please enter the y position to be placed at");
                        y_value = Integer.parseInt(stdIn.readLine());
                        move = new int[] { x_value, y_value };
                    } else {
                        mcts1.search(hex1.getState());
                        move = mcts1.returnBestMove(hex1.getState());

                        x_value = move[0];
                        y_value = move[1];
                    }

                    coord = ("(" + x_value + "," + y_value + ");");
                    System.out.println(coord);
                    hex1.play(move);
                    hex1.printBoard();
                    hex1.connectWithNeighbors(move);
                    if (hex1.getWinner() != 0) {
                        done = true;
                    }

                    playerToPlay = true;
                } else {
                    if (!AI_2) {
                        System.out.println("Please enter the x position to be placed at");
                        x_value = Integer.parseInt(stdIn.readLine());

                        System.out.println("Please enter the y position to be placed at");
                        y_value = Integer.parseInt(stdIn.readLine());
                        move = new int[] { x_value, y_value };
                    } else {
                        mcts2.search(hex2.getState());
                        move = mcts2.returnBestMove(hex2.getState());

                        x_value = move[0];
                        y_value = move[1];
                    }

                    coord = ("(" + x_value + "," + y_value + ");");
                    System.out.println(coord);
                    hex2.play(move);
                    hex2.printBoard();
                    hex2.connectWithNeighbors(move);
                    if (hex2.getWinner() != 0) {
                        done = true;
                    }

                    playerToPlay = false;
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while reading in coords");
        }

        if (playerToPlay) {
            System.out.println("Player 2 wins!");
        }
        else {
            System.out.println("Player 1 wins!");
        }
    }
}