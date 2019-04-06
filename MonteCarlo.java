import java.util.*;

class MonteCarlo {
    private Game game;
    private Random random = new Random();

    // Storing states so that we can continue exploration when next state arrives
    // Tree of different game states
    private Map<Integer, TreeNode> nodes = new HashMap<Integer, TreeNode>();

    MonteCarlo(Game game) {
        this.game = game;
    }

    // This one initializes and creates the root node which represents the initial state of the game
    public void initializeNode(int[][] state) {
        Integer stateHash = hash(state);
        if(!nodes.containsKey(stateHash)) {
            List<int[]> unexploredMoves = game.getAvailableMoves();
            TreeNode rootNode = new TreeNode(null, game, null, unexploredMoves);
            nodes.put(stateHash, rootNode);
        }
    }

    public static Integer hash(int[][] state) {
        return Arrays.deepHashCode(state);
    }

    public static void printBoard(int[][] state) {
        for (int y = 0; y < state.length; y++) {
            for(int i = 0; i < y; i++) {
                System.out.print(" ");
            }

            for (int x = 0; x < state[y].length; x++) {
                if(state[x][y] == -1) {
                    System.out.print(2);
                } else {
                    System.out.print(state[x][y]);
                }
                
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    // Initiating the search method
    // ROBUST version
    public void search(int[][] state) {
        initializeNode(state);

        // 1000 searches --> will be changed to time based search later
        for(int i = 0; i < 100000; i++) {
            System.out.println(i);
            // Select best node based on UCB1 evaluation equation
            TreeNode node = select(state);
            int winner = node.game.getWinner();

            // Expand if node is a leaf and has no winners
            if (!node.isLeaf() && winner == 0) {
                node = expand(node);
                winner = simulate(node);
            }
            // Update the other nodes
            this.backpropagation(node, winner);
        }
    }

    // Returns the best move from the tree based on the node that had the most simulations
    public int[] returnBestMove(int[][] state) {
        TreeNode node = nodes.get(hash(state));
        List<int[]> allMoves = node.getAllMoves();
        
        int[] bestMove = new int[2];
        int mostMoves = Integer.MIN_VALUE;

        for(int[] move : allMoves) {
            TreeNode childNode = node.children.get(TreeNode.hash(move));
            if(childNode == null) {
                continue;
            }
            if (childNode.numRollouts > mostMoves) {
                bestMove = move;
                System.out.println(((double) childNode.numWins)/childNode.numRollouts);
                mostMoves = childNode.numRollouts;
            }
        }

        return bestMove;
    }

    // Phase 1
    public TreeNode select(int[][] state) {
        TreeNode node = nodes.get(hash(state));

        while(node.isFullyExpanded() && !node.isLeaf()) {
            List<int[]> possibleMoves = node.getAllMoves();
            int[] bestMove = new int[2];
            double bestUCB1 = Integer.MIN_VALUE;

            for(int[] move : possibleMoves) {
                TreeNode child = node.children.get(TreeNode.hash(move));
                double childValue = child.getUCB1();

                if(childValue > bestUCB1) {
                    bestUCB1 = childValue;
                    bestMove = move;
                }
            }

            node = node.children.get(TreeNode.hash(bestMove));
        }

        return node;
    }

    // Phase 2
    public TreeNode expand(TreeNode node) {
        List<int[]> unexploredMoves = node.getUnexploredMoves();

        int index = random.nextInt(unexploredMoves.size());
        int[] move = unexploredMoves.get(index);

        Game childGame = game.getNextState(move);
        List<int[]> childUnexploredMoves = childGame.getAvailableMoves();
        TreeNode childNode = new TreeNode(node, childGame, move, childUnexploredMoves);
        nodes.put(hash(childGame.getState()), childNode);
        node.children.put(TreeNode.hash(move), childNode);

        return childNode;
    }

    // Phase 3
    // In the simulation we can add heurestic so that the program blocks winning moves when encountered
    // So check if opponent is one move away from winning, if yes then block that move
    // And if you're one move away from winning then take that move
    public int simulate(TreeNode node) {
        Game game = node.game;
        int winner = game.getWinner();

        while (winner == 0) {
            List<int[]> availableMoves = game.getAvailableMoves();
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

            if(node.game.currentPlayer == -winner) {
                node.numWins += 1;
            }

            node = node.parent;
        }
    }
}