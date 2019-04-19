import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class MonteCarlo {    
    static long selectTime = 0;
    static long checkWinner = 0;
    static long expansionTime = 0;
    static long simulationTime = 0;
    static long propagationTime = 0;

    static long gettingUnexploredMoves = 0;
    static long gettingNextState = 0;
    static long gettingAvailableMoves = 0;

    private Game game;
    private Random random = new Random();

    // Storing states so that we can continue exploration when next state arrives
    // Tree of different game states
    ConcurrentHashMap<String, TreeNode> nodes = new ConcurrentHashMap<String, TreeNode>();
    ConcurrentHashMap<String, TreeNode> bootstrapNodes = new ConcurrentHashMap<String, TreeNode>();

    MonteCarlo(Game game) {
        this.game = game;
    }

    MonteCarlo(Game game, ConcurrentHashMap bootstrapNodes) {
        this.game = game;
        this.bootstrapNodes = bootstrapNodes;
    }

    // This one initializes and creates the root node which represents the initial state of the game
    public void initializeNode(int[][] state) {
        String stateHash = hash(state);
        if(!nodes.containsKey(stateHash)) {
            List<int[]> unexploredMoves;
            TreeNode memoryNode = bootstrapNodes.getOrDefault(stateHash, null);

            if(memoryNode != null) {
                unexploredMoves = memoryNode.getAllMoves();
            } else {
                unexploredMoves = game.getAvailableMoves();
            }
            
            TreeNode rootNode = new TreeNode(null, game, null, unexploredMoves);

            if(memoryNode != null) {
                // System.out.println("Node retrieved from memory! Reusing its data");
                rootNode.numRollouts += memoryNode.numRollouts;
                // System.out.println(memoryNode.numRollouts);
                rootNode.numWins += memoryNode.numWins;
                // System.out.println(memoryNode.numWins);
            }

            nodes.put(stateHash, rootNode);
        }
    }

    public static String hash(int[][] state) {
        String hash = "";
        for(int x = 0; x < state.length; x++) {
            for(int y = 0; y < state[x].length; y++) {
                hash += state[x][y];
            }
        }
        return hash;
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
    static int i = 0;
    public void search(int[][] state) {
        initializeNode(state);

        // 5 seconds thinking time -> hex blitz mode
        long start = System.currentTimeMillis();
        i = 0;
        while(false || (System.currentTimeMillis()-start) < 2000) {
            new SimulationThread(state, this).start();

            // System.out.println(i);
            // Select best node based on UCB1 evaluation equation
            // long startTime = System.nanoTime();
            // TreeNode node = select(state);
            // long endTime = System.nanoTime();
            // selectTime += (endTime - startTime);

            // startTime = System.nanoTime();
            // int winner = node.game.getWinner();
            // endTime = System.nanoTime();
            // checkWinner += (endTime - startTime);

            // Expand if node is a leaf and has no winners
            // if (!node.isLeaf() && winner == 0) {
            //     startTime = System.nanoTime();
            //     node = expand(node);
            //     endTime = System.nanoTime();
            //     expansionTime += (endTime - startTime);

            //     startTime = System.nanoTime();
            //     winner = simulate(node);
            //     endTime = System.nanoTime();
            //     simulationTime += (endTime - startTime);
            // }
            // // Update the other nodes
            // startTime = System.nanoTime();
            // this.backpropagation(node, winner);
            // endTime = System.nanoTime();
            // propagationTime += (endTime - startTime);   
        }

        // System.out.format("%s took %s\n", "Select", selectTime); // Pretty fast here as well
        // System.out.format("%s took %s\n", "Expansion", expansionTime); // Can be improved imo (took 0.16 secs in total)
        // System.out.format("%s took %s\n", "Check winner", checkWinner); // Check winner is also fast and efficient
        // System.out.format("%s took %s\n", "Simulation", simulationTime); // This one is the slow animal
        // System.out.format("%s took %s\n", "Propagation", propagationTime); // Propagation is very fast and efficient
        
        // System.out.println();
        // System.out.format("%s took %s\n", "Unex Moves", gettingUnexploredMoves);
        // System.out.format("%s took %s\n", "Next State", gettingNextState);
        //System.out.format("%s took %s\n", "Available Moves", gettingAvailableMoves);

        System.out.println();
        System.out.format("%s took %s\n", "One Move Win", Hex.checkingOneMoveVictory); // heuristics only check when largest subset has certain size
        System.out.format("%s took %s\n", "Two Move Win", Hex.checkingTwoMoveVictory); // This method seems to be taking up most of the time
        // Adding minimum set size seems to have improved the algorithm by a lot
        // System.out.format("%s took %s\n", "Enhanced", Hex.enhancedTime);
        // System.out.format("%s took %s\n", "Non-Enhanced", Hex.nonEnhancedTime);

        System.out.println();
        System.out.format("%s took %s\n", "Total Next State", Hex.nextStateTime);
        //System.out.format("%s took %s\n", "Disjoint Maps", Hex.disjointMapTime);
        System.out.format("%s took %s\n", "Copy state", Hex.copyStateTime);
        System.out.format("%s took %s\n", "Connect time", Hex.connectTime);

        System.out.println();
        System.out.println(i);
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
        long startTime = System.nanoTime();
        List<int[]> unexploredMoves = node.getUnexploredMoves();
        long endTime = System.nanoTime();
        gettingUnexploredMoves += endTime - startTime;

        int index = random.nextInt(unexploredMoves.size());
        int[] move = unexploredMoves.get(index);

        startTime = System.nanoTime();
        Game childGame = game.getNextState(move);
        endTime = System.nanoTime();
        gettingNextState += endTime - startTime;


        TreeNode memoryNode = bootstrapNodes.getOrDefault(hash(childGame.getState()), null);
        List<int[]> childUnexploredMoves = null;
        if(memoryNode != null) {
            childUnexploredMoves = memoryNode.getAllMoves();
        } else {
            childUnexploredMoves = childGame.getAvailableMoves();
        }

        TreeNode childNode = new TreeNode(node, childGame, move, childUnexploredMoves);

        if(memoryNode != null) {
            // System.out.println("Node retrieved from memory! Reusing its data");
            childNode.numRollouts += memoryNode.numRollouts;
            // System.out.println(memoryNode.numRollouts);
            childNode.numWins += memoryNode.numWins;
            // System.out.println(memoryNode.numWins);
        }

        nodes.putIfAbsent(hash(childGame.getState()), childNode);
        node.children.put(TreeNode.hash(move), childNode);

        return childNode;
    }

    // Phase 3
    // In the simulation we can add heurestic so that the program blocks winning moves when encountered
    // So check if opponent is one move away from winning, if yes then block that move
    // And if you're one move away from winning then take that move
    public int simulate(TreeNode node) {
        int[][] nextState = Hex.copyState(node.game.getState());
        int currentPlayer = node.game.currentPlayer;
        int[] lastMove = new int[] {((Hex) node.game).lastMove[0], ((Hex) node.game).lastMove[1]};
        Game game = new Hex(nextState, currentPlayer, lastMove, new HashMap<Integer,Set<Integer>>(((Hex) node.game).disjointSets));
        int winner = game.getWinner();

        while (winner == 0) {
            List<int[]> availableMoves = game.getAvailableMoves(false);
            // System.out.println(availableMoves.toString());
            int[] move = availableMoves.get(random.nextInt(availableMoves.size()));
            game.play(move);
            ((Hex) game).connectWithNeighbors(move);
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