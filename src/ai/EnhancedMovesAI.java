package ai;

import game.*;
import java.util.*;

public class EnhancedMovesAI extends Hex {

    public EnhancedMovesAI() {
        super();
    }

    public EnhancedMovesAI(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        super(state, currentPlayer, lastMove, disjointSets);
    }

    @Override
    public Game copyGame() {
        int[][] nextState = Hex.copyState(this.getState());
        int[] lastMove = new int[] {this.lastMove[0], this.lastMove[1]};
        Game game = new EnhancedMovesAI(nextState, this.currentPlayer, lastMove, new HashMap<Integer,Set<Integer>>(this.disjointSets));

        return game;
    }

    @Override
    public List<int[]> getSmartMoves() {
        return this.getSmartMoves(true, Integer.MAX_VALUE);
    }

    @Override
    public List<int[]> getSmartMoves(boolean enhanced, int rollouts) {
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        if(enhanced) {
            // Initial board search only consider the initial 3x3 square in the middle of the board
            for (int x = (BOARD_SIZE-1)/2 - 1; x <= (BOARD_SIZE-1)/2 + 1; x++) {
                for (int y = (BOARD_SIZE-1)/2 - 1; y <= (BOARD_SIZE-1)/2 + 1; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
    
            possibleMoves.addAll(this.getTilesToCheck());
        } else {
            for (int x = 0; x < BOARD_SIZE; x++) {
                for (int y = 0; y < BOARD_SIZE; y++) {
                    if(state[x][y] == 0) {
                        possibleMoves.add(new int[] {x, y});
                    }
                }
            }
        }

        return new ArrayList<int[]>(possibleMoves);
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
        EnhancedMovesAI newGame = new EnhancedMovesAI(nextState, nextPlayer, move, new HashMap<Integer,Set<Integer>>(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }
}