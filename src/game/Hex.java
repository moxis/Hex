package game;

import java.util.*;
import java.io.*;

/**
 * Class to represent Hex
 */
public class Hex extends Game {
    public static final int BOARD_SIZE = 11;
    public static int[][] DEFAULT_BOARD = new int[BOARD_SIZE][BOARD_SIZE];

    public static List<Set<Integer>> playerOneEdges = new ArrayList<Set<Integer>>();
    public static List<Set<Integer>> playerTwoEdges = new ArrayList<Set<Integer>>();

    public Map<Integer, Set<Integer>> disjointSets = new HashMap<>();
    public int[] lastMove;

    /**
     * One of the constructors for the Hex class.
     */
    public Hex() {
        super(DEFAULT_BOARD, 1);
    }

    /**
     * One of the constructors for the Hex class.
     * 
     * @param state         2d array of integers representing the current state of
     *                      the game
     * @param currentPlayer integer representig the current player of the game.
     */
    public Hex(int[][] state, int currentPlayer) {
        super(state, currentPlayer);
    }

    /**
     * One of the constructors for the Hex class.
     * 
     * @param state         2d array of integers representing the current state of
     *                      the game
     * @param currentPlayer integer representig the current player of the game.
     * @param lastMove      Integer array representing the last move that was made
     * @param disjointSets  Map of integers and a set of intgers used to represents
     *                      the current sets of hex placements that are used to find
     *                      if a user has won.
     */
    public Hex(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        this(state, currentPlayer);
        this.lastMove = lastMove;
        this.disjointSets = disjointSets;
    }

    /**
     * Initialises the necessary conditions for a user having won.
     */
    public static void initializeWinConditions() {
        Set<Integer> top = new HashSet<Integer>();
        Set<Integer> bottom = new HashSet<Integer>();
        Set<Integer> right = new HashSet<Integer>();
        Set<Integer> left = new HashSet<Integer>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            top.add(hash(new int[] { i, 0 }));
            bottom.add(hash(new int[] { i, BOARD_SIZE - 1 }));

            right.add(hash(new int[] { BOARD_SIZE - 1, i }));
            left.add(hash(new int[] { 0, i }));
        }

        playerOneEdges.add(top);
        playerOneEdges.add(bottom);

        playerTwoEdges.add(right);
        playerTwoEdges.add(left);
    }

    /**
     * Used to copy the current game class.
     */
    public Game copyGame() {
        int[][] nextState = Hex.copyState(this.getState());
        int[] lastMove = new int[] { this.lastMove[0], this.lastMove[1] };
        Game game = new Hex(nextState, this.currentPlayer, lastMove,
                new HashMap<Integer, Set<Integer>>(this.disjointSets));

        return game;
    }

    /**
     * Checks if a piece is connected with any of its neighbors
     * 
     * @param move Integer array representing the move that has been made
     */
    public void connectWithNeighbors(int[] move) {
        int currentPlayer = this.getState()[move[0]][move[1]];
        for (int[] neighbour : this.getNeighbours(move)) {
            if (this.getState()[neighbour[0]][neighbour[1]] == currentPlayer) {
                union(hash(move), hash(neighbour));
            }
        }
    }

    /**
     * 
     * @param a
     * @param b
     */
    public void union(Integer a, Integer b) {
        disjointSets.get(a).addAll(disjointSets.get(b));
        for (Integer key : disjointSets.get(b)) {
            disjointSets.put(key, disjointSets.get(a));
        }
    }

    // Need this method because arrays can't be used reliably as keys
    public static Integer hash(int[] arr) {
        return Arrays.hashCode(arr);
    }

    /**
     * Returns the list of smart moves to be made by the AI.
     */
    public List<int[]> getSmartMoves() {
        return this.getSmartMoves(true, Integer.MAX_VALUE);
    }

    /**
     * Finds bridges to be taken into account by the AI (a bridge is when there is
     * connection that has not been placed yet, but cannot be broken by the opponent
     * in a single turn).
     * 
     * @param move Integer array representing the move that has been made
     * @return Returns a list of arrays of integers representing the existing
     *         bridges
     */
    public List<int[]> getBridges(int[] move) {
        List<int[]> bridges = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        // // 1
        // if (x + 1 < BOARD_SIZE && y - 2 >= 0) {
        //     bridges.add(new int[] { x + 1, y - 2 });
        // }

        // // 2
        // if (x + 2 < BOARD_SIZE && y - 1 >= 0) {
        //     bridges.add(new int[] { x + 2, y - 1 });
        // }

        // // 3
        // if (x + 1 < BOARD_SIZE && y + 1 < BOARD_SIZE) {
        //     bridges.add(new int[] { x + 1, y + 1 });
        // }

        // // 4
        // if (x - 1 >= 0 && y + 2 < BOARD_SIZE) {
        //     bridges.add(new int[] { x - 1, y + 2 });
        // }

        // // 5
        // if (x - 2 >= 0 && y + 1 < BOARD_SIZE) {
        //     bridges.add(new int[] { x - 2, y + 1 });
        // }

        // // 6
        // if (x - 1 >= 0 && y - 1 >= 0) {
        //     bridges.add(new int[] { x - 1, y - 1 });
        // }

        if (x - 1 >= 0 && y - 2 >= 0) {
            bridges.add(new int[] { x - 1, y - 2 });
        }

        if (x + 1 < BOARD_SIZE && y - 1 >= 0) {
            bridges.add(new int[] { x + 1, y - 1 });
        }

        if (x + 2 < BOARD_SIZE && y + 1 < BOARD_SIZE) {
            bridges.add(new int[] { x + 2, y + 1 });
        }

        if (x + 1 < BOARD_SIZE && y + 2 < BOARD_SIZE) {
            bridges.add(new int[] { x + 1, y + 2 });
        }

        if (x - 1 >= 0 && y + 1 < BOARD_SIZE) {
            bridges.add(new int[] { x - 1, y + 1 });
        }

        if (x - 2 >= 0 && y - 1 >= 0) {
            bridges.add(new int[] { x - 2, y - 1 });
        }

        return bridges;
    }

    /**
     * Gets the connections between two points.
     * 
     * @param a Array of integers representing the first point
     * @param b Array of integers representing the second point
     * @return Return the list of arrays of integers representing the existing
     *         connections.
     */
    public List<int[]> getConnections(int[] a, int[] b) {
        List<int[]> connections = new ArrayList<int[]>();

        List<int[]> neighboursOfA = this.getNeighbours(a);
        Set<Integer> neighboursOfB = new HashSet<>();

        for (int[] neighbour : this.getNeighbours(b)) {
            neighboursOfB.add(hash(neighbour));
        }

        for (int[] neighbour : neighboursOfA) {
            if (neighboursOfB.contains(hash(neighbour))) {
                connections.add(neighbour);
            }
        }

        return connections;
    }

    /**
     * Finds bridges that should be connected before the opponent closes them
     * 
     * @return Returns the list of arrays of integers of bridges that need to be
     *         completed
     */
    public List<int[]> getBridgeToDefend() {
        int[][] state = this.getState();
        List<int[]> bridges = new ArrayList<int[]>();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (state[x][y] == this.currentPlayer) {
                    for (int[] bridge : this.getBridges(new int[] { x, y })) {
                        if (state[bridge[0]][bridge[1]] == this.currentPlayer) {
                            List<int[]> connections = this.getConnections(new int[] { x, y }, bridge);
                            if (connections.size() == 2) {
                                if (state[connections.get(0)[0]][connections.get(0)[1]] == -this.currentPlayer) {
                                    if (state[connections.get(1)[0]][connections.get(1)[1]] == 0) {
                                        bridges.add(connections.get(1));
                                    }
                                }

                                if (state[connections.get(1)[0]][connections.get(1)[1]] == -this.currentPlayer) {
                                    if (state[connections.get(0)[0]][connections.get(0)[1]] == 0) {
                                        bridges.add(connections.get(0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return bridges;
    }

    /**
     * Minimax AI heuristics that increase efficiency and find possiblle better
     * moves
     * 
     * @param possibleMoves Set of arrays of integers for possible moves to be
     *                      looked at
     * @return Returns a list of arrays of Strings representing the moves that could
     *         be made.
     */
    public List<int[]> checkMinimax(Set<int[]> possibleMoves) {
        int[][] state = this.getState();

        // Remove the neighbor thing because even tho it speeds it up, the AI is more
        // dumb now
        // Neighbours only good for checking victorious moves not for two step thinking
        // moves
        // For two step thinking moves, the second move should utilize neighbours
        Set<int[]> currentNeighbours = this.getAllAvailableNeighbours(BOARD_SIZE / 2);

        this.currentPlayer *= -1;
        Set<int[]> theirNeighbours = this.getAllAvailableNeighbours(BOARD_SIZE / 2);
        this.currentPlayer *= -1;

        // Tiny version of minimax to add some heuristics
        for (int[] move1 : currentNeighbours) { // Checking for victory
            Game game1 = this.getNextState(move1);
            if (game1.getWinner() != 0) {
                return new ArrayList<int[]>(Arrays.asList(move1));
            }
        }

        this.currentPlayer *= -1; // Checking for enemy victory
        for (int[] move1 : theirNeighbours) {
            Game game1 = this.getNextState(move1);
            if (game1.getWinner() != 0) {
                this.currentPlayer *= -1;
                return new ArrayList<int[]>(Arrays.asList(move1));
            }
        }
        this.currentPlayer *= -1;

        for (int[] move1 : possibleMoves) {
            Game game1 = this.getNextState(move1, false);
            int winCount = 0; // If wincount is 2, then it's unblockable
            for (int[] move2 : possibleMoves) {
                if (state[move2[0]][move2[1]] != 0 && move1 != move2) {
                    Game game2 = game1.getNextState(move2);
                    if (game2.getWinner() != 0) {
                        winCount += 1;
                    }

                    if (winCount == 2) {
                        return new ArrayList<int[]>(Arrays.asList(move1));
                    }
                }
            }
        }

        this.currentPlayer *= -1;
        for (int[] move1 : possibleMoves) {
            Game game1 = this.getNextState(move1, false);
            int winCount = 0; // If wincount is 2, then it's unblockable so block it
            for (int[] move2 : possibleMoves) {
                if (state[move2[0]][move2[1]] != 0 && move1 != move2) {
                    Game game2 = game1.getNextState(move2);
                    if (game2.getWinner() != 0) {
                        winCount += 1;
                    }
                    if (winCount == 2) {
                        this.currentPlayer *= -1;
                        return new ArrayList<int[]>(Arrays.asList(move1));
                    }
                }
            }
        }
        this.currentPlayer *= -1;

        return null;
    }

    /**
     * Generates a list of moves to be considered
     * 
     * @return Set of arrays of Integers that represents the moves to be
     *         investigated
     */
    public Set<int[]> getEnhancedMoves() {
        int[][] state = this.getState();
        Set<int[]> possibleMoves = new HashSet<>();
        // Initial board search only consider the initial 3x3 square in the middle of
        // the board
        for (int x = (BOARD_SIZE - 1) / 2 - 1; x <= (BOARD_SIZE - 1) / 2 + 1; x++) {
            for (int y = (BOARD_SIZE - 1) / 2 - 1; y <= (BOARD_SIZE - 1) / 2 + 1; y++) {
                if (state[x][y] == 0) {
                    possibleMoves.add(new int[] { x, y });
                }
            }
        }

        possibleMoves.addAll(this.getTilesToCheck());

        return possibleMoves;
    }

    /**
     * Used to generate a list of moves to be made, taking into account all of the
     * previous options for moves
     * 
     * @param enhanced Boolean representing whether enhanced moves are to be checked
     *                 or not
     * @param rollouts Integer that means if a certain number of operatiosn have
     *                 occurred more checks, such as that for bridges, are made
     * @return Set of arrays of Integers that represents the moves to be
     *         investigated
     */
    public List<int[]> getSmartMoves(boolean enhanced, int rollouts) {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (state[x][y] == 0) {
                    possibleMoves.add(new int[] { x, y });
                }
            }
        }

        // Heavy playouts only applied on heavier nodes
        if (rollouts > 300) {
            List<int[]> temp = checkMinimax(possibleMoves);
            if (temp != null) {
                return temp;
            }

            // Saving bridges if one is being attacked
            List<int[]> defend = this.getBridgeToDefend();
            if (defend.size() != 0) {
                return defend;
            }
        }

        if (enhanced) {
            possibleMoves = getEnhancedMoves();
        }

        return new ArrayList<int[]>(possibleMoves);
    }

    /**
     * Finds the tiles that are to be checked, so that the program doesn't waste
     * time checking tiles that will not generate useful moves
     * 
     * @return Set of arrays of integers representing the moves to be investigated
     */
    public Set<int[]> getTilesToCheck() {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (state[x][y] != 0) {
                    for (int i = x - 3; i <= x + 3; i++) {
                        for (int j = y - 3; j <= y + 3; j++) {
                            try {
                                if (state[i][j] == 0) {
                                    possibleMoves.add(new int[] { i, j });
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                            }
                        }
                    }
                }
            }
        }

        return possibleMoves;
    }

    /**
     * Finds the available neighbours that need to be checked
     * 
     * @param min Integer representing the minimum size to be investigated
     * @return Set of arrays of integers representing the moves to be investigated
     */
    public Set<int[]> getAllAvailableNeighbours(int min) {
        Set<int[]> allNeighbours = new HashSet<int[]>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (state[x][y] == this.currentPlayer) {
                    if (this.disjointSets.get(hash(new int[] { x, y })).size() >= min) {
                        for (int[] neighbour : this.getNeighbours(new int[] { x, y })) {
                            if (state[neighbour[0]][neighbour[1]] == 0) {
                                allNeighbours.add(neighbour);
                            }
                        }
                    }
                }
            }
        }

        return allNeighbours;
    }

    /**
     * Used to find the next possible state
     * 
     * @param move Integer array representing the move that has been made
     * @return returns a Game object representing the next game state
     */
    public Game getNextState(int[] move) {
        return this.getNextState(move, true);
    }

    // HashMap of sets have to be passed as well
    /**
     * Used to find the next state to be assigned as the current state of the game
     * 
     * @param move          Integer array representing the move that has been made
     * @param changePlayers Boolean that will change the player if a move has been
     *                      committed
     * @return Returns a game object to be assigned as the current game state
     */
    public Game getNextState(int[] move, boolean changePlayers) {
        int[][] nextState = copyState(this.getState());
        nextState[move[0]][move[1]] = currentPlayer;

        int nextPlayer = currentPlayer;
        if (changePlayers) {
            nextPlayer *= -1;
        }
        Hex newGame = new Hex(nextState, nextPlayer, move, new HashMap<Integer, Set<Integer>>(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }

    // Disjoint-set data structure
    // https://en.wikipedia.org/wiki/Disjoint-set_data_structure
    /**
     * Finds if a player has made a winning move
     */
    public int getWinner() {
        if (lastMove == null) {
            return 0;
        }

        Set<Integer> blob = disjointSets.get(hash(lastMove));
        List<Set<Integer>> winningSet;
        if (currentPlayer == -1) {
            winningSet = playerOneEdges;
        } else {
            winningSet = playerTwoEdges;
        }

        if (intersection(blob, winningSet.get(0)) && intersection(blob, winningSet.get(1))) {
            return currentPlayer * -1;
        }

        return 0;
    }

    /**
     * Used to find if there is an intersection between two sets
     * 
     * @param a Set of integers representing the first path to be checked
     * @param b Set of integers representing the second path to be checked
     * @return
     */
    public static boolean intersection(Set<Integer> a, Set<Integer> b) {
        if (a.size() > b.size()) {
            return intersection(b, a);
        }

        for (Integer elem : a) {
            if (b.contains(elem)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Used to play the game by inputting an array of integers representing the move
     * to be made
     * 
     * @param move Array of integers represnting the move to be made.
     */
    public void play(int[] move) {
        int[][] state = this.getState();
        try {
            if (state[move[0]][move[1]] == 0) {
                state[move[0]][move[1]] = currentPlayer;

                Set<Integer> temp = new HashSet<Integer>();
                temp.add(hash(move));
                disjointSets.put(hash(move), temp);
                lastMove = move;
                currentPlayer *= -1;
            } else {
                System.out.println("Invalid move.. Please try again");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid move.. Please try again");
        }

    }

    /**
     * Finds if a move has connected neighbours in order to add them to sets
     * 
     * @param move Array of integers represnting the move to be made.
     * @return returns a list of arrays of integers representing the neighbours of
     *         the move that had been made.
     */
    public List<int[]> getNeighbours(int[] move) {
        List<int[]> neighbours = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        // code for board flipped the other way
        /*
         * // 1 if (x - 1 >= 0) { neighbours.add(new int[] {x - 1, y}); }
         * 
         * if (x + 1 < BOARD_SIZE) { neighbours.add(new int[] {x + 1, y}); }
         * 
         * // 2 if (y - 1 >= 0) { neighbours.add(new int[] {x, y - 1}); }
         * 
         * if (y + 1 < BOARD_SIZE) { neighbours.add(new int[] {x, y + 1}); }
         * 
         * // 3 if (x - 1 >= 0 && y + 1 < BOARD_SIZE) { neighbours.add(new int[] {x - 1,
         * y + 1}); }
         * 
         * if (x + 1 < BOARD_SIZE && y - 1 >= 0) { neighbours.add(new int[] {x + 1, y -
         * 1}); }
         */

        if (x - 1 >= 0 && y - 1 >= 0) {
            neighbours.add(new int[] { x - 1, y - 1 });
        }

        if (y - 1 >= 0) {
            neighbours.add(new int[] { x, y - 1 });
        }

        if (x + 1 < BOARD_SIZE) {
            neighbours.add(new int[] { x + 1, y });
        }

        if (x + 1 < BOARD_SIZE && y + 1 < BOARD_SIZE) {
            neighbours.add(new int[] { x + 1, y + 1 });
        }

        if (y + 1 < BOARD_SIZE) {
            neighbours.add(new int[] { x, y + 1 });
        }

        if (x - 1 >= 0) {
            neighbours.add(new int[] { x - 1, y });
        }

        return neighbours;
    }

    /**
     * Method used to print out the current board
     */
    public void printBoard() {
        int[][] state = this.getState();
        System.out.print("                ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print("\u001B[31m" + i + "\u001B[0m ");
        }
        System.out.println();

        System.out.print("                ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print("\u001B[31m■■\u001B[0m");
        }
        System.out.println();

        for (int y = 0; y < BOARD_SIZE; y++) {
            if (y < 10) {
                System.out.print("\u001B[34m" + y + "\u001B[0m  ");
            } else {
                System.out.print("\u001B[34m" + y + "\u001B[0m ");
            }

            for (int i = y; i < BOARD_SIZE; i++) {
                System.out.print(" ");
            }
            System.out.print("\u001B[34m▮ \u001B[0m");

            for (int x = 0; x < BOARD_SIZE; x++) {
                if (state[x][y] == 1) {
                    System.out.print("\u001B[31m⬤\u001B[0m");
                } else if (state[x][y] == -1) {
                    System.out.print("\u001B[34m⬤\u001B[0m");
                } else {
                    System.out.print("⬤");
                }
                System.out.print(" ");
            }
            System.out.print("\u001B[34m▮\u001B[0m");
            System.out.println();
        }

        System.out.print("      ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print("\u001B[31m■■\u001B[0m");
        }
        System.out.println();
    }
}