import java.util.*;

public class Hex extends Game {
    static int[][] DEFAULT_BOARD = new int[11][11];
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

    static long disjointMapTime = 0;
    public static Map<Integer, Set<Integer>> copyDisjointMap(Map<Integer, Set<Integer>> disjointSets) {
        long startTime = System.nanoTime();
        Map<Integer, Set<Integer>> newDisjointSets = new HashMap<>();

        for (Integer i : disjointSets.keySet()) {
            if (!newDisjointSets.containsKey(i)) {
                Set<Integer> temp = new HashSet<Integer>();
                temp.addAll(disjointSets.get(i));
                for(Integer j : temp) {
                    if (!newDisjointSets.containsKey(j)) {
                        newDisjointSets.put(j, temp);
                    }   
                }
            }
        }
        // Map<Integer, Set<Integer>> newDisjointSets = cloner.deepClone(disjointSets);
        long endTime = System.nanoTime();
        disjointMapTime += endTime - startTime;
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
        MonteCarlo mcts2 = new MonteCarlo(hex);
        int winner = 0;
        // mcts.search(hex.getState());
        while(winner == 0) {
            int[] move;
            if (hex.currentPlayer == -1) {
                /*int x = reader.nextInt();
                int y = reader.nextInt();
                move = new int[] {x, y};*/
                System.out.println("Kanishk");
                mcts2.search(hex.getState());
                move = mcts2.returnBestMove(hex.getState());
            } else {
                System.out.println("Jessica");
                mcts.search(hex.getState());
                move = mcts.returnBestMove(hex.getState());
            }
            System.out.println(move[0]);
            System.out.println(move[1]);
            hex.play(move);
            hex.printBoard();
            hex.connectWithNeighbors(move);
            winner = hex.getWinner();
            // mcts.search(hex.getState());
        }

        System.out.print("Winner: ");
        if(winner == 1) {
            System.out.println("Jessica");
        } else {
            System.out.println("Kanishk");
        }
    }

    static long connectTime = 0;
    public void connectWithNeighbors(int[] move) {
        long startTime = System.nanoTime();
        int currentPlayer = this.getState()[move[0]][move[1]];
        for (int[] neighbour : this.getNeighbours(move)) {
            if (this.getState()[neighbour[0]][neighbour[1]] == currentPlayer) {
                union(hash(move), hash(neighbour));
            }
        }
        long endTime = System.nanoTime();
        connectTime += endTime - startTime;
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
        return this.getAvailableMoves(true);
    }


    static long checkingOneMoveVictory = 0;
    static long checkingTwoMoveVictory = 0;
    static long enhancedTime = 0;
    static long nonEnhancedTime = 0;
    
    public List<int[]> getAvailableMoves(boolean enhanced) {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        // Remove the neighbor thing because even tho it speeds it up, the AI is more dumb now
        // Neighbours only good for checking victorious moves not for two step thinking moves
        // For two step thinking moves, the second move should utilize neighbours
        Set<int[]> currentNeighbours = this.getAllAvailableNeighbours(DEFAULT_BOARD.length / 2);

        this.currentPlayer *= -1;
        Set<int[]> theirNeighbours = this.getAllAvailableNeighbours(DEFAULT_BOARD.length / 2);
        this.currentPlayer *= -1;

        long startTime = System.nanoTime();
        // Tiny version of minimax to add some heuristics
        for(int[] move1 : currentNeighbours) { // Checking for victory
            Game game1 = this.getNextState(move1);
            if (game1.getWinner() != 0) {
                long endTime = System.nanoTime();
                checkingOneMoveVictory += endTime - startTime;
                return new ArrayList<int[]>(Arrays.asList(move1));
            }
        }
                        
        this.currentPlayer *= -1; // Checking for enemy victory
        for(int[] move1 : theirNeighbours) {
            Game game1 = this.getNextState(move1);
            if (game1.getWinner() != 0) {
                this.currentPlayer *= -1;
                long endTime = System.nanoTime();
                checkingOneMoveVictory += endTime - startTime;
                return new ArrayList<int[]>(Arrays.asList(move1));
            }
        }
        this.currentPlayer *= -1;

        long endTime = System.nanoTime();
        checkingOneMoveVictory += endTime - startTime;
        
        startTime = System.nanoTime();
        for(int[] move1 : currentNeighbours) {
            Game game1 = this.getNextState(move1, false);
            int winCount = 0; // If wincount is 2, then it's unblockable
            for(int[] move2 : this.getNeighbours(move1)) { // getting neighbours helped reduce by 15 seconds
                if (state[move2[0]][move2[1]] != 0) {
                    Game game2 = game1.getNextState(move2);
                    if (game2.getWinner() != 0) {
                        winCount += 1;
                    }

                    if (winCount == 2) {
                        endTime = System.nanoTime();
                        checkingTwoMoveVictory += endTime - startTime;
                        return new ArrayList<int[]>(Arrays.asList(move1));
                    }
                }
            }
        }

        this.currentPlayer *= -1;
        for(int[] move1 : theirNeighbours) {
            Game game1 = this.getNextState(move1, false);
            int winCount = 0; // If wincount is 2, then it's unblockable so block it
            for(int[] move2 : this.getNeighbours(move1)) {
                if (state[move2[0]][move2[1]] != 0) {
                    Game game2 = game1.getNextState(move2);
                    if (game2.getWinner() != 0) {
                        winCount += 1;
                    }
                    if (winCount == 2) {
                        this.currentPlayer *= -1;
                        endTime = System.nanoTime();
                        checkingTwoMoveVictory += endTime - startTime;
                        return new ArrayList<int[]>(Arrays.asList(move1));
                    }
                }
            }
        }
        this.currentPlayer *= -1;
        endTime = System.nanoTime();
        checkingTwoMoveVictory += endTime - startTime;

        if(enhanced) {
            startTime = System.nanoTime();
            // Initial board search
            for (int x = (state.length-1)/2 - 1; x <= (state.length-1)/2 + 1; x++) {
                for (int y = (state[x].length-1)/2 - 1; y <= (state[x].length-1)/2 + 1; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
    
            for (int[] tile : this.getTilesToCheck()) {
                boolean match = false;
                for (int[] move : possibleMoves) {
                    if (tile[0] == move[0] && tile[1] == move[1]) {
                        match = true;
                    }
                }
    
                if(!match) {
                    possibleMoves.add(tile);
                }
            }

            endTime = System.nanoTime();
            enhancedTime += endTime - startTime;
        } else {
            startTime = System.nanoTime();
            for (int x = 0; x < state.length; x++) {
                for (int y = 0; y < state[x].length; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
            endTime = System.nanoTime();
            nonEnhancedTime += endTime - startTime;
        }


        return new ArrayList<int[]>(possibleMoves);
    }

    public Set<int[]> getTilesToCheck() {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if(state[x][y] != 0) {
                    for (int i = x - 2; i <= x + 2; i++) {
                        for (int j = y - 2; j <= y + 2; j++) {
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

        for(int x = 0; x < state.length; x++) {
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

    static long copyStateTime = 0;
    public static int[][] copyState(int[][] state) {
        long startTime = System.nanoTime();
        int[][] newState = new int[state.length][state[0].length];
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                newState[i][j] = state[i][j];
            }
        }
        long endTime = System.nanoTime();
        copyStateTime += endTime - startTime;
        return newState;
    }

    public Game getNextState(int[] move) {
        return this.getNextState(move, true);
    }

    static long nextStateTime = 0;

    // HashMap of sets have to be passed as well
    public Game getNextState(int[] move, boolean changePlayers) {
        long startTime = System.nanoTime();
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

        long endTime = System.nanoTime();
        nextStateTime += endTime - startTime;
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
            winningSet = p1Edges;
        } else {
            winningSet = p2Edges;
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