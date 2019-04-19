import java.util.*;
import game.*;
import ai.*;


public class Main {
    public static void main(String[] args) {
        Hex.initializeWinConditions();

        Hex hex = new Hex();
        SaveBridgeAI hex2 = new SaveBridgeAI();

        hex.printBoard();
        Scanner reader = new Scanner(System.in);

        MonteCarlo mcts = new MonteCarlo(hex, true, false, 2000);
        MonteCarlo mcts2 = new MonteCarlo(hex2, true, false, 2000);

        Random random = new Random();
        int winner = 0;
        int start;
        if (random.nextDouble() < 0.5) {
            start = 1;
        } else {
            start = -1;
        }

        while(winner == 0) {
            int[] move;
            if (hex2.currentPlayer == start) {
                /*int x = reader.nextInt();
                int y = reader.nextInt();
                move = new int[] {x, y};*/
                System.out.println(hex2.getClass().getName());
                mcts2.search(hex2.getState());
                move = mcts2.returnBestMove(hex2.getState());
                /*List<int[]> moves = hex.getAvailableMoves();
                int i = random.nextInt(moves.size());
                move = moves.get(i);*/
            } else {
                System.out.println(hex.getClass().getName());
                mcts.search(hex.getState());
                move = mcts.returnBestMove(hex.getState());
            }
            // System.out.println(move[0]);
            // System.out.println(move[1]);
            
            hex.play(move);
            hex2.play(move);
            hex.printBoard();
            hex.connectWithNeighbors(move);
            hex2.connectWithNeighbors(move);
            winner = hex.getWinner();
            // mcts.search(hex.getState());
        }

        System.out.print("Winner: ");
        if(winner == start) {
            System.out.println(hex2.getClass().getName());
        } else {
            System.out.println(hex.getClass().getName());
        }

        //mcts2.bootstrapNodes.putAll(mcts2.nodes);
        //saveTreeToFile(mcts2.bootstrapNodes);
    }
}