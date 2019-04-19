import java.util.*;

public class Main {
    public static void main(String[] args) {
        Hex.initializeWinConditions();

        Hex hex = new Hex();
        hex.printBoard();

        Scanner reader = new Scanner(System.in);


        MonteCarlo mcts = new MonteCarlo(hex, true, false, 2000);
        MonteCarlo mcts2 = new MonteCarlo(hex, true, true, 2000);
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
            if (hex.currentPlayer == start) {
                /*int x = reader.nextInt();
                int y = reader.nextInt();
                move = new int[] {x, y};*/
                System.out.println("Joann");
                mcts2.search(hex.getState());
                move = mcts2.returnBestMove(hex.getState());
                /*List<int[]> moves = hex.getAvailableMoves();
                int i = random.nextInt(moves.size());
                move = moves.get(i);*/
            } else {
                System.out.println("Jessica");
                mcts.search(hex.getState());
                move = mcts.returnBestMove(hex.getState());
            }
            // System.out.println(move[0]);
            // System.out.println(move[1]);
            hex.play(move);
            hex.printBoard();
            hex.connectWithNeighbors(move);
            winner = hex.getWinner();
            // mcts.search(hex.getState());
        }

        System.out.print("Winner: ");
        if(winner == start) {
            System.out.println("Joann");
        } else {
            System.out.println("Jessica");
        }

        //mcts2.bootstrapNodes.putAll(mcts2.nodes);
        //saveTreeToFile(mcts2.bootstrapNodes);
    }
}