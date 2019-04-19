import java.util.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.*;

public class Hex extends Game {
    static final int BOARD_SIZE = 11;
    static int[][] DEFAULT_BOARD = new int[BOARD_SIZE][BOARD_SIZE];

    static List<Set<Integer>> playerOneEdges = new ArrayList<Set<Integer>>();
    static List<Set<Integer>> playerTwoEdges = new ArrayList<Set<Integer>>();

    Map<Integer, Set<Integer>> disjointSets = new HashMap<>();
    int[] lastMove;

    Hex() {
        super(DEFAULT_BOARD, 1);
    }

    Hex(int[][] state, int currentPlayer) {
        super(state, currentPlayer);
    }

    Hex(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
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

    public static int[][] copyState(int[][] state) {
        int[][] newState = new int[state.length][state[0].length];
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                newState[i][j] = state[i][j];
            }
        }

        return newState;
    }

    public Game getNextState(int[] move) {
        return this.getNextState(move, true);
    }

    static long nextStateTime = 0;

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
    }

    public List<int[]> getNeighbours(int[] move) {
        List<int[]> neighbours = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        // 1
        if (x - 1 >= 0) {
            neighbours.add(new int[] {x - 1, y});
        }

        if (x + 1 < this.getState().length) {
            neighbours.add(new int[] {x + 1, y});
        }

        // 2
        if (y - 1 >= 0) {
            neighbours.add(new int[] {x, y - 1});
        }

        if (y + 1 < this.getState()[0].length) {
            neighbours.add(new int[] {x, y + 1});

        }

        // 3
        if (x - 1 >= 0 && y + 1 < this.getState()[0].length) {
            neighbours.add(new int[] {x - 1, y + 1});
        }

        if (x + 1 < this.getState().length && y - 1 >= 0) {
            neighbours.add(new int[] {x + 1, y - 1});
        }

        return neighbours;
    }

    public void printBoard() {
        int[][] state = this.getState();
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
}