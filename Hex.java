import java.util.*;

public class Hex extends Game {
    static int[][] DEFAULT_BOARD = new int[3][3];
    Map<Integer, Set<Integer>> disjointSets = new HashMap<>();
    int[] lastMove;

    static List<Set<Integer>> p1Edges = new ArrayList<Set<Integer>>();
    static List<Set<Integer>> p2Edges = new ArrayList<Set<Integer>>();

    Hex(int[][] state, int currentPlayer) {
        super(state, currentPlayer);
    }

    Hex(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        this(state, currentPlayer);
        this.lastMove = lastMove;
        this.disjointSets = disjointSets;
    }

    public static Map<Integer, Set<Integer>> copyDisjointMap(Map<Integer, Set<Integer>> disjointSets) {
        Map<Integer, Set<Integer>> newDisjointSets = new HashMap<>();

        for (Integer i : disjointSets.keySet()) {
            Set<Integer> temp = new HashSet<Integer>();
            temp.addAll(disjointSets.get(i));
            newDisjointSets.put(i, temp);
        }

        return newDisjointSets;
    }

    public static void main(String[] args) {
        Hex hex = new Hex(DEFAULT_BOARD, 1);
        hex.printBoard();

        Scanner reader = new Scanner(System.in);

        Set<Integer> top = new HashSet<Integer>();
        Set<Integer> bottom = new HashSet<Integer>();
        Set<Integer> right = new HashSet<Integer>();
        Set<Integer> left = new HashSet<Integer>();

        for (int i = 0; i < DEFAULT_BOARD.length; i++) {
            top.add(hash(new int[] {i, 0}));
            bottom.add(hash(new int[] {i, DEFAULT_BOARD.length - 1}));

            right.add(hash(new int[] {DEFAULT_BOARD.length - 1, i}));
            left.add(hash(new int[] {0, i}));
        }
        p1Edges.add(top);
        p1Edges.add(bottom);

        p2Edges.add(right);
        p2Edges.add(left);

        MonteCarlo mcts = new MonteCarlo(hex);
        int winner = 0;
        while(winner == 0) {
            int[] move;
            if (hex.currentPlayer == 1) {
                int x = reader.nextInt();
                int y = reader.nextInt();
                move = new int[] {x, y};
            } else {
                System.out.println("my turn!");
                mcts.search(hex.getState());
                move = mcts.returnBestMove(hex.getState());
            }
            System.out.println(move[0]);
            System.out.println(move[1]);
            hex.play(move);
            hex.printBoard();
            hex.connectWithNeighbors(move);
            winner = hex.getWinner();
            //System.out.println(winner);
        }
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

    public List<int[]> getAvailableMoves() {
        List<int[]> possibleMoves = new ArrayList<>();

        int[][] state = this.getState();
        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if(state[x][y] == 0) {
                    possibleMoves.add(new int[] {x, y});
                }
            }
        }

        return possibleMoves;
    }

    public int[][] copyState(int[][] state) {
        int[][] newState = new int[state.length][state[0].length];

        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                newState[i][j] = state[i][j];
            }
        }

        return newState;
    }

    // HashMap of sets have to be passed as well
    public Game getNextState(int[] move) {
        int[][] nextState = copyState(this.getState());
        nextState[move[0]][move[1]] = currentPlayer;
        Hex newGame = new Hex(nextState, currentPlayer * -1, move, copyDisjointMap(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }

    // Disjoint-set data structure
    // https://en.wikipedia.org/wiki/Disjoint-set_data_structure
    public int getWinner() {
        Set<Integer> blob = disjointSets.get(hash(lastMove));
        List<Set<Integer>> winningSet;
        if (currentPlayer == -1) {
            winningSet = p1Edges;
        } else {
            winningSet = p2Edges;
        }

        if (intersection(blob, winningSet.get(0)) && intersection(blob, winningSet.get(1))) {
            return currentPlayer *= -1;
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