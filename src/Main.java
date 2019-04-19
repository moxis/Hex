import java.util.*;
import game.*;
import ai.*;

// https://challonge.com/tournaments/bracket_generator?ref=Fw8r6YRzll
public class Main {
    public static void main(String[] args) {
        Hex.initializeWinConditions();

        NoHeuristicsAI hex = new NoHeuristicsAI();
        SaveBridgeWithMinimaxAI hex2 = new SaveBridgeWithMinimaxAI();

        MonteCarlo mcts = new MonteCarlo(hex, true, false, 5000);
        MonteCarlo mcts2 = new MonteCarlo(hex2, true, false, 5000);

        int winner = 0;
        int start;

        Random random = new Random();
        if (random.nextDouble() < 0.5) {
            start = 1;
        } else {
            start = -1;
        }

        while(winner == 0) {
            int[] move;
            if (hex2.currentPlayer == start) {
                System.out.println(hex2.getClass().getName());
                mcts2.search(hex2.getState());
                move = mcts2.returnBestMove(hex2.getState());
            } else {
                System.out.println(hex.getClass().getName());
                mcts.search(hex.getState());
                move = mcts.returnBestMove(hex.getState());
            }
            
            hex.play(move);
            hex2.play(move);

            hex.printBoard();
            
            hex.connectWithNeighbors(move);
            hex2.connectWithNeighbors(move);
            winner = hex.getWinner();
        }

        System.out.print("Winner: ");
        if(winner == start) {
            System.out.println(hex2.getClass().getName());
        } else {
            System.out.println(hex.getClass().getName());
        }
    }
}