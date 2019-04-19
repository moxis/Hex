package ai;

import java.util.*;
import game.*;

public class MinimaxHybridAI extends Hex {

    public MinimaxHybridAI() {
        super();
    }

    public MinimaxHybridAI(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        super(state, currentPlayer, lastMove, disjointSets);
    }

    @Override
    public Game copyGame() {
        int[][] nextState = Hex.copyState(this.getState());
        int[] lastMove = new int[] {this.lastMove[0], this.lastMove[1]};
        Game game = new MinimaxHybridAI(nextState, this.currentPlayer, lastMove, new HashMap<Integer,Set<Integer>>(this.disjointSets));

        return game;
    }

    @Override
    public List<int[]> getSmartMoves() {
        return this.getSmartMoves(false, Integer.MAX_VALUE);
    }

    @Override
    public List<int[]> getSmartMoves(boolean enhanced, int rollouts) {
        List<int[]> possibleMoves = new ArrayList<>();
        int[][] state = this.getState();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if(state[x][y] == 0) {
                    possibleMoves.add(new int[] {x, y});
                }
            }
        }
        
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
        }

        return possibleMoves;
    }

    @Override
    public Game getNextState(int[] move) {
        return this.getNextState(move, true);
    }

    // HashMap of sets have to be passed as well
    @Override
    public Game getNextState(int[] move, boolean changePlayers) {
        int[][] nextState = copyState(this.getState());
        nextState[move[0]][move[1]] = currentPlayer;

        int nextPlayer = currentPlayer;
        if (changePlayers) {
            nextPlayer *= -1;
        }
        MinimaxHybridAI newGame = new MinimaxHybridAI(nextState, nextPlayer, move, new HashMap<Integer,Set<Integer>>(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }
}