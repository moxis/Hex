import java.util.*;

public class FirstAI extends AIEngine {
    Hex game;

    public FirstAI(Game game) {
        this.game = (Hex) game;
    }

    public List<int[]> getBridgeToDefend() {
        int[][] state = game.getState();
        List<int[]> bridges = new ArrayList<int[]>();

        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if(state[x][y] == game.currentPlayer) {
                    for(int[] bridge : this.getBridges(new int[] {x, y})) {
                        if(state[bridge[0]][bridge[1]] == game.currentPlayer) {
                            List<int[]> connections = this.getConnections(new int[] {x, y}, bridge);
                            if(connections.size() == 2) {
                                if (state[connections.get(0)[0]][connections.get(0)[1]] == -game.currentPlayer) {
                                    if (state[connections.get(1)[0]][connections.get(1)[1]] == 0) {
                                        bridges.add(connections.get(1));
                                    }
                                }
    
                                if (state[connections.get(1)[0]][connections.get(1)[1]] == -game.currentPlayer) {
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

    public List<int[]> getBridges(int[] move) {
        List<int[]> bridges = new ArrayList<int[]>();
        int x = move[0];
        int y = move[1];

        int maxLength = game.getState().length;

        // 1
        if (x + 1 < maxLength && y - 2 >= 0) {
            bridges.add(new int[] {x + 1, y - 2});
        }

        // 2
        if (x + 2 < maxLength && y - 1 >= 0) {
            bridges.add(new int[] {x + 2, y - 1});
        }

        // 3
        if (x + 1 < maxLength && y + 1 < maxLength) {
            bridges.add(new int[] {x + 1, y + 1});
        }

        // 4
        if (x - 1 >= 0 && y + 2 < maxLength) {
            bridges.add(new int[] {x - 1, y + 2});
        }

        // 5
        if (x - 2 >= 0 && y + 1 < maxLength) {
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

        List<int[]> neighboursA = game.getNeighbours(a);
        Set<Integer> neighboursB = new HashSet<>();

        for(int[] neighbour : game.getNeighbours(b)) {
            neighboursB.add(hash(neighbour));
        }

        for(int[] neighbour : neighboursA) {
            if (neighboursB.contains(hash(neighbour))) {
                connections.add(neighbour);
            }
        }

        return connections;
    }

    public static Integer hash(int[] arr) {
        return Arrays.hashCode(arr);
    }

    public List<int[]> getAvailableMoves(boolean enhanced, int rollouts) {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = game.getState();

        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state.length; y++) {
                if(state[x][y] == 0) {
                    possibleMoves.add(new int[] {x, y});
                }
            }
        }

        if(rollouts > 1000) {
            Set<int[]> currentNeighbours = this.getAllAvailableNeighbours(Hex.BOARD_SIZE / 2);

            game.currentPlayer *= -1;
            Set<int[]> theirNeighbours = this.getAllAvailableNeighbours(Hex.BOARD_SIZE / 2);
            game.currentPlayer *= -1;

            // Tiny version of minimax to add some heuristics
            for(int[] move1 : currentNeighbours) { // Checking for victory
                Game game1 = game.getNextState(move1);
                if (game1.getWinner() != 0) {
                    return new ArrayList<int[]>(Arrays.asList(move1));
                }
            }
                            
            game.currentPlayer *= -1; // Checking for enemy victory
            for(int[] move1 : theirNeighbours) {
                Game game1 = game.getNextState(move1);
                if (game1.getWinner() != 0) {
                    game.currentPlayer *= -1;
                    return new ArrayList<int[]>(Arrays.asList(move1));
                }
            }
            game.currentPlayer *= -1;

            for(int[] move1 : possibleMoves) {
                Game game1 = game.getNextState(move1, false);
                int winCount = 0; // If wincount is 2, then it's unblockable
                for(int[] move2 : possibleMoves) { // getting neighbours helped reduce by 15 seconds
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

            game.currentPlayer *= -1;
            for(int[] move1 : possibleMoves) {
                Game game1 = game.getNextState(move1, false);
                int winCount = 0; // If wincount is 2, then it's unblockable so block it
                for(int[] move2 : possibleMoves) {
                    if (state[move2[0]][move2[1]] != 0 && move1 != move2) {
                        Game game2 = game1.getNextState(move2);
                        if (game2.getWinner() != 0) {
                            winCount += 1;
                        }
                        if (winCount == 2) {
                            game.currentPlayer *= -1;
                            return new ArrayList<int[]>(Arrays.asList(move1));
                        }
                    }
                }
            }
            game.currentPlayer *= -1;

            List<int[]> defend = this.getBridgeToDefend();
            if(defend.size() != 0) {
                return defend;
            }
        }

        if(enhanced) {
            possibleMoves = new HashSet<>();
            //startTime = System.nanoTime();
            // Initial board search
            for (int x = (state.length-1)/2 - 1; x <= (state.length-1)/2 + 1; x++) {
                for (int y = (state[x].length-1)/2 - 1; y <= (state[x].length-1)/2 + 1; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
    
            possibleMoves.addAll(this.getTilesToCheck());
        } else {
            return new ArrayList<int[]>(possibleMoves);
        }

        return new ArrayList<int[]>(possibleMoves);
    }

    public Set<int[]> getTilesToCheck() {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = game.getState();

        for (int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if(state[x][y] != 0) {
                    for (int i = x - 4; i <= x + 4; i++) {
                        for (int j = y - 4; j <= y + 4; j++) {
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
        int[][] state = game.getState();

        for(int x = 0; x < state.length; x++) {
            for (int y = 0; y < state[x].length; y++) {
                if (state[x][y] == game.currentPlayer) {
                    if (game.disjointSets.get(hash(new int[] {x, y})).size() >= min) {
                        for (int[] neighbour : game.getNeighbours(new int[] {x, y})) {
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
}