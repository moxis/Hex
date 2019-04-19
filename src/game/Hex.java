package game;

import java.util.*;
import java.io.*;

public class Hex extends Game {
    public static final int BOARD_SIZE = 11;
    public static int[][] DEFAULT_BOARD = new int[BOARD_SIZE][BOARD_SIZE];

    public static List<Set<Integer>> playerOneEdges = new ArrayList<Set<Integer>>();
    public static List<Set<Integer>> playerTwoEdges = new ArrayList<Set<Integer>>();

    public Map<Integer, Set<Integer>> disjointSets = new HashMap<>();
    public int[] lastMove;

    public Hex() {
        super(DEFAULT_BOARD, 1);
    }

    public Hex(int[][] state, int currentPlayer) {
        super(state, currentPlayer);
    }

    public Hex(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        this(state, currentPlayer);
        this.lastMove = lastMove;
        this.disjointSets = disjointSets;
    }

    public static void initializeWinConditions() {
        Set<Integer> top = new HashSet<Integer>();
        Set<Integer> bottom = new HashSet<Integer>();
        Set<Integer> right = new HashSet<Integer>();
        Set<Integer> left = new HashSet<Integer>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            top.add(hash(new int[] {i, 0}));
            bottom.add(hash(new int[] {i, BOARD_SIZE - 1}));

            right.add(hash(new int[] {BOARD_SIZE - 1, i}));
            left.add(hash(new int[] {0, i}));
        }

        playerOneEdges.add(top);
        playerOneEdges.add(bottom);

        playerTwoEdges.add(right);
        playerTwoEdges.add(left);
    }

    public Game copyGame() {
        int[][] nextState = Hex.copyState(this.getState());
        int[] lastMove = new int[] {this.lastMove[0], this.lastMove[1]};
        Game game = new Hex(nextState, this.currentPlayer, lastMove, new HashMap<Integer,Set<Integer>>(this.disjointSets));

        return game;
    }

    public void connectWithNeighbors(int[] move) {
        int currentPlayer = this.getState()[move[0]][move[1]];
        for (int[] neighbour : this.getNeighbours(move)) {
            if (this.getState()[neighbour[0]][neighbour[1]] == currentPlayer) {
                union(hash(move), hash(neighbour));
            }
        }
    }

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

    public List<int[]> getSmartMoves() {
        return this.getSmartMoves(true, Integer.MAX_VALUE);
    }
    
    public List<int[]> getBridges(int[] move) {
        List<int[]> bridges = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        // 1
        if (x + 1 < BOARD_SIZE && y - 2 >= 0) {
            bridges.add(new int[] {x + 1, y - 2});
        }

        // 2
        if (x + 2 < BOARD_SIZE && y - 1 >= 0) {
            bridges.add(new int[] {x + 2, y - 1});
        }

        // 3
        if (x + 1 < BOARD_SIZE && y + 1 < BOARD_SIZE) {
            bridges.add(new int[] {x + 1, y + 1});
        }

        // 4
        if (x - 1 >= 0 && y + 2 < BOARD_SIZE) {
            bridges.add(new int[] {x - 1, y + 2});
        }

        // 5
        if (x - 2 >= 0 && y + 1 < BOARD_SIZE) {
            bridges.add(new int[] {x - 2, y + 1});
        }

        // 6
        if (x - 1 >= 0 && y - 1 >= 0) {
            bridges.add(new int[] {x - 1, y - 1});
        }

        return bridges;
    }

    public List<int[]> getConnections(int[] a, int[] b) {
        List<int[]> connections = new ArrayList<int[]>();

        List<int[]> neighboursOfA = this.getNeighbours(a);
        Set<Integer> neighboursOfB = new HashSet<>();

        for(int[] neighbour : this.getNeighbours(b)) {
            neighboursOfB.add(hash(neighbour));
        }

        for(int[] neighbour : neighboursOfA) {
            if (neighboursOfB.contains(hash(neighbour))) {
                connections.add(neighbour);
            }
        }

        return connections;
    }

    public List<int[]> getBridgeToDefend() {
        int[][] state = this.getState();
        List<int[]> bridges = new ArrayList<int[]>();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if(state[x][y] == this.currentPlayer) {
                    for(int[] bridge : this.getBridges(new int[] {x, y})) {
                        if(state[bridge[0]][bridge[1]] == this.currentPlayer) {
                            List<int[]> connections = this.getConnections(new int[] {x, y}, bridge);
                            if(connections.size() == 2) {
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

    public List<int[]> getSmartMoves(boolean enhanced, int rollouts) {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if(state[x][y] == 0) {
                    possibleMoves.add(new int[] {x, y});
                }
            }
        }

        // Heavy playouts only applied on heavier nodes
        if(rollouts > 300) {
            // Remove the neighbor thing because even tho it speeds it up, the AI is more dumb now
            // Neighbours only good for checking victorious moves not for two step thinking moves
            // For two step thinking moves, the second move should utilize neighbours
            Set<int[]> currentNeighbours = this.getAllAvailableNeighbours(BOARD_SIZE / 2);

            this.currentPlayer *= -1;
            Set<int[]> theirNeighbours = this.getAllAvailableNeighbours(BOARD_SIZE / 2);
            this.currentPlayer *= -1;

            // Tiny version of minimax to add some heuristics
            for(int[] move1 : currentNeighbours) { // Checking for victory
                Game game1 = this.getNextState(move1);
                if (game1.getWinner() != 0) {
                    return new ArrayList<int[]>(Arrays.asList(move1));
                }
            }
                            
            this.currentPlayer *= -1; // Checking for enemy victory
            for(int[] move1 : theirNeighbours) {
                Game game1 = this.getNextState(move1);
                if (game1.getWinner() != 0) {
                    this.currentPlayer *= -1;
                    return new ArrayList<int[]>(Arrays.asList(move1));
                }
            }
            this.currentPlayer *= -1;

            for(int[] move1 : possibleMoves) {
                Game game1 = this.getNextState(move1, false);
                int winCount = 0; // If wincount is 2, then it's unblockable
                for(int[] move2 : possibleMoves) {
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
            for(int[] move1 : possibleMoves) {
                Game game1 = this.getNextState(move1, false);
                int winCount = 0; // If wincount is 2, then it's unblockable so block it
                for(int[] move2 : possibleMoves) {
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

            // Saving bridges if one is being attacked
            List<int[]> defend = this.getBridgeToDefend();
            if(defend.size() != 0) {
                return defend;
            }
        }

        if(enhanced) {
            possibleMoves = new HashSet<>();
            // Initial board search only consider the initial 3x3 square in the middle of the board
            for (int x = (BOARD_SIZE-1)/2 - 1; x <= (BOARD_SIZE-1)/2 + 1; x++) {
                for (int y = (BOARD_SIZE-1)/2 - 1; y <= (BOARD_SIZE-1)/2 + 1; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
    
            possibleMoves.addAll(this.getTilesToCheck());
        }

        return new ArrayList<int[]>(possibleMoves);
    }

    public Set<int[]> getTilesToCheck() {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if(state[x][y] != 0) {
                    for (int i = x - 3; i <= x + 3; i++) {
                        for (int j = y - 3; j <= y + 3; j++) {
                            try {
                                if(state[i][j] == 0) {
                                    possibleMoves.add(new int[] {i, j});
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {}
                        }
                    }
                }
            }
        }

        return possibleMoves;
    }

    public Set<int[]> getAllAvailableNeighbours(int min) {
        Set<int[]> allNeighbours = new HashSet<int[]>();
        int[][] state = this.getState();

        for(int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if (state[x][y] == this.currentPlayer) {
                    if (this.disjointSets.get(hash(new int[] {x, y})).size() >= min) {
                        for (int[] neighbour : this.getNeighbours(new int[] {x, y})) {
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

    public Game getNextState(int[] move) {
        return this.getNextState(move, true);
    }

    // HashMap of sets have to be passed as well
    public Game getNextState(int[] move, boolean changePlayers) {
        int[][] nextState = copyState(this.getState());
        nextState[move[0]][move[1]] = currentPlayer;

        int nextPlayer = currentPlayer;
        if (changePlayers) {
            nextPlayer *= -1;
        }
        Hex newGame = new Hex(nextState, nextPlayer, move, new HashMap<Integer,Set<Integer>>(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }

    // Disjoint-set data structure
    // https://en.wikipedia.org/wiki/Disjoint-set_data_structure
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

    public static boolean intersection(Set<Integer> a, Set<Integer> b) {
        if (a.size() > b.size()) {
            return intersection(b, a);
        }

        for(Integer elem : a) {
            if(b.contains(elem)) {
                return true;
            }
        }

        return false;
    }

    // TODO: add move validations
    public void play(int[] move) {
        int[][] state = this.getState();
        if (state[move[0]][move[1]] == 0 ) {
            state[move[0]][move[1]] = currentPlayer;

            Set<Integer> temp = new HashSet<Integer>();
            temp.add(hash(move));
            disjointSets.put(hash(move), temp);
            lastMove = move;
            currentPlayer *= -1;
        } else {
            System.out.println("Invalid move.. Please try again");
        }
    }

    public List<int[]> getNeighbours(int[] move) {
        List<int[]> neighbours = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        // 1
        if (x - 1 >= 0) {
            neighbours.add(new int[] {x - 1, y});
        }

        if (x + 1 < BOARD_SIZE) {
            neighbours.add(new int[] {x + 1, y});
        }

        // 2
        if (y - 1 >= 0) {
            neighbours.add(new int[] {x, y - 1});
        }

        if (y + 1 < BOARD_SIZE) {
            neighbours.add(new int[] {x, y + 1});

        }

        // 3
        if (x - 1 >= 0 && y + 1 < BOARD_SIZE) {
            neighbours.add(new int[] {x - 1, y + 1});
        }

        if (x + 1 < BOARD_SIZE && y - 1 >= 0) {
            neighbours.add(new int[] {x + 1, y - 1});
        }

        return neighbours;
    }

    public void printBoard() {
        int[][] state = this.getState();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for(int i = 0; i < y; i++) {
                System.out.print(" ");
            }

            for (int x = 0; x < BOARD_SIZE; x++) {
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
}