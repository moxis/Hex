import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import game.*;

class MonteCarlo {    
    private Game game;
    private Random random = new Random();

    // Storing states so that we can continue exploration when next state arrives
    // Tree of different game states
    Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();
    static List<int[]> emptyArray = new ArrayList<int[]>();

    private boolean rave = false;
    private boolean multithread = false;
    private int thinkingTime;

    MonteCarlo(Game game) {
        this.game = game;
    }

    MonteCarlo(Game game, boolean rave, boolean multithread, int thinkingTime) {
        this.game = game;
        this.rave = rave;
        this.multithread = multithread;
        this.thinkingTime = thinkingTime;
    }

    // This one initializes and creates the root node which represents the initial state of the game
    public void initializeNode(int[][] state) {
        String stateHash = hash(state);
        if(!nodes.containsKey(stateHash)) {
            List<int[]> unexploredMoves = game.getSmartMoves();
            
            TreeNode rootNode = new TreeNode(null, game, null, unexploredMoves);
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
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        i = 0;
        while(false || (System.currentTimeMillis()-start) < thinkingTime) {
            if(multithread) {
                if(executor.getQueue().size() < 500) {
                    try {
                        Runnable thread = new SimulationThread(state, this);
                        executor.execute(thread);
                    } catch(Exception e) {e.printStackTrace();};
                }
            } else {
                try {
                    // Select best node based on UCB1 evaluation equation
                    TreeNode node = select(state);
                    int winner = node.game.getWinner();
                    Object[] results = new Object[] {winner, emptyArray, emptyArray};

                    if (!node.isLeaf() && winner == 0) {
                        node = this.expand(node);
                        results = this.simulate(node);
                        winner = (int) results[0];
                    }

                    // Update the other nodes
                    this.backpropagation(node, winner, (List<int[]>) results[1], (List<int[]>) results[2]);
                    i += 1;
                } catch (Exception e) {
                    System.out.println("Single");
                    e.printStackTrace();
                }
            }
        }
        executor.shutdownNow();
        while (!executor.isTerminated()) {
        }
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
            int[] bestMove = possibleMoves.get(0);
            double bestUCB1 = Integer.MIN_VALUE;

            for(int[] move : possibleMoves) {
                TreeNode child = node.children.get(TreeNode.hash(move));
                double childValue = child.getUCB1(this.rave);
                
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
        
        while(unexploredMoves.size() == 0) {
            return null;
        }

        int index = random.nextInt(unexploredMoves.size());
        int[] move = unexploredMoves.get(index);

        Game childGame = game.getNextState(move);
        List<int[]> childUnexploredMoves = childGame.getSmartMoves();
        TreeNode childNode = new TreeNode(node, childGame, move, childUnexploredMoves);

        nodes.put(hash(childGame.getState()), childNode);
        node.children.put(TreeNode.hash(move), childNode);

        return childNode;
    }

    // Phase 3
    // In the simulation we can add heurestic so that the program blocks winning moves when encountered
    // So check if opponent is one move away from winning, if yes then block that move
    // And if you're one move away from winning then take that move
    public Object[] simulate(TreeNode node) {
        List<int[]> firstRavePoints = new ArrayList<int[]>();
        List<int[]> secondRavePoints = new ArrayList<int[]>();

        // Creating copy of the game before simulation
        Game game = node.game.copyGame();
        Integer winner = game.getWinner();

        while (winner == 0) {
            List<int[]> availableMoves = game.getSmartMoves(false, node.numRollouts);

            int[] move = availableMoves.get(random.nextInt(availableMoves.size()));
            if(game.currentPlayer == 1) {
                firstRavePoints.add(move);
            } else {
                secondRavePoints.add(move);
            }

            game.play(move);
            ((Hex) game).connectWithNeighbors(move);
            winner = game.getWinner();
        }

        /*int[][] state = game.getState();
        for(int x = 0; x < state.length; x++) {
            for(int y = 0; y < state.length; y++) {
                if(state[x][y] == 1) {
                    firstRavePoints.add(new int[] {x, y});
                } else if (state[x][y] == -1) {
                    secondRavePoints.add(new int[] {x, y});
                }
            }
        }*/

        return new Object[] {winner, firstRavePoints, secondRavePoints};
    }

    // Phase 4
    public void backpropagation(TreeNode node, int winner, List<int[]> firstRavePoints, List<int[]> secondRavePoints) {
        List<int[]> points;
        while(node != null) {
            if(node.game.currentPlayer == -1) {
                points = firstRavePoints;
            } else {
                points = secondRavePoints;
            }

            for(int[] move : points) {
                TreeNode child = node.children.getOrDefault(TreeNode.hash(move), null);
                if (child != null) {
                    child.numRaveRollouts += 1;
                    if(node.game.currentPlayer == -winner) {
                        child.numRaveWins += 1;
                    }
                }
            }

            node.numRollouts += 1;
            if(node.game.currentPlayer == -winner) {
                node.numWins += 1;
            }
            node = node.parent;
        }
    }
}