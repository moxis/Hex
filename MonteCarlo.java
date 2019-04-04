import java.util.*;

class MonteCarlo {
    private Game game;
    private int exploreParam;

    // Storing states so that we can continue exploration when next state arrives
    private Map<int[][], TreeNode> nodes = new HashMap<int[][], TreeNode>();

    MonteCarlo(Game game, int exploreParam) {
        this.game = game;
        this.exploreParam = exploreParam;
    }

    public void search(int[][] state) {
        TreeNode node = select(state);
        int winner = node.game.getWinner();

        for(int i = 0; i < 1000; i++) {
            if (node.isLeaf() && winner == 0) {
                node = expand(node);
                winner = simulate(node);
            }
            this.backpropagation(node, winner);
        }
    }

    public int[] returnBestMove(int[][] state) {
        TreeNode node = nodes.get(state);
        int[] bestPlay = new int[2];
        Set<int[]> allPlays = node.getAllMoves();
        int mostMoves = Integer.MIN_VALUE;

        for(int[] play : allPlays) {
            TreeNode childNode = node.children.get(play);
            if (childNode.numRollouts > mostMoves) {
                bestPlay = play;
                mostMoves = childNode.numRollouts;
            }
        }

        return bestPlay;
    }

    // Phase 1
    public TreeNode select(int[][] state) {
        TreeNode node = nodes.get(state);

        while(node.isFullyExpanded() && !node.isLeaf()) {
            Set<int[]> possibleMoves = node.getAllMoves();
            int[] bestMove = new int[2];
            double bestUCB1 = Integer.MIN_VALUE;

            for(int[] move : possibleMoves) {
                TreeNode child = node.children.get(move);
                double childValue = child.getUCB1();

                if(childValue > bestUCB1) {
                    bestUCB1 = childValue;
                    bestMove = move;
                }
            }

            node = node.children.get(bestMove);
        }

        return node;
    }

    // Phase 2
    public TreeNode expand(TreeNode node) {
        List<int[]> unexploredMoves = node.getUnexploredMoves();
        int index = new Random().nextInt(unexploredMoves.size());
        int[] move = unexploredMoves.get(index);

        Game childGame = game.getNextState(move);
        List<int[]> childUnexploredMoves = childGame.getAvailableActions();
        TreeNode childNode = new TreeNode(node, childGame, move, childUnexploredMoves);
        nodes.put(childGame.getState(), childNode);

        return childNode;
    }

    // Phase 3
    // In the simulation we can add heurestic so that the program blocks winning moves when encountered
    // So check if opponent is one move away from winning, if yes then block that move
    public int simulate(TreeNode node) {
        Game game = node.game;
        int winner = game.getWinner();

        while (winner == 0) {
            List<int[]> availableMoves = game.getAvailableActions();
            int[] move = availableMoves.get(new Random().nextInt(availableMoves.size()));
            game = game.getNextState(move);
            winner = game.getWinner();
        }

        return winner;
    }

    // Phase 4
    public void backpropagation(TreeNode node, int winner) {
        while(node != null) {
            node.numRollouts += 1;

            if(node.game.currentPlayer == winner) {
                node.numWins += 1;
            }

            node = node.parent;
        }
    }
}