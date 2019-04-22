package ai;

import game.*;
import java.util.*;

public class EnhancedMovesWithSaveBridgeAI extends Hex {

    public EnhancedMovesWithSaveBridgeAI(int[][] state, int currentPlayer) {
        super(state, currentPlayer);
    }

    public EnhancedMovesWithSaveBridgeAI() {
        super();
    }

    public EnhancedMovesWithSaveBridgeAI(int[][] state, int currentPlayer, int[] lastMove, Map<Integer, Set<Integer>> disjointSets) {
        super(state, currentPlayer, lastMove, disjointSets);
    }

    @Override
    public Game copyGame() {
        int[][] nextState = Hex.copyState(this.getState());
        int[] lastMove = new int[] {this.lastMove[0], this.lastMove[1]};
        Game game = new EnhancedMovesWithSaveBridgeAI(nextState, this.currentPlayer, lastMove, new HashMap<Integer,Set<Integer>>(this.disjointSets));

        return game;
    }

    @Override
    public List<int[]> getSmartMoves() {
        return this.getSmartMoves(true, Integer.MAX_VALUE);
    }

    @Override
    public List<int[]> getSmartMoves(boolean enhanced, int rollouts) {
        if(rollouts > 300) {
            // Saving bridges if one is being attacked
            List<int[]> defend = this.getBridgeToDefend();
            if(defend.size() != 0) {
                return defend;
            }
        }
        
        Set<int[]> possibleMoves = new HashSet<>();
        int[][] state = this.getState();

        if(enhanced) {
            possibleMoves = this.getEnhancedMoves();
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
        EnhancedMovesWithSaveBridgeAI newGame = new EnhancedMovesWithSaveBridgeAI(nextState, nextPlayer, move, new HashMap<Integer,Set<Integer>>(disjointSets));

        Set<Integer> temp = new HashSet<Integer>();
        temp.add(hash(move));
        newGame.disjointSets.put(hash(move), temp);
        newGame.connectWithNeighbors(move);

        return newGame;
    }
}