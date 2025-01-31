package game;

import java.util.*;

/**
 * Class used to perform operations on the game
 */
public abstract class Game {
    private int[][] state;
    public int currentPlayer;

    /**
     * Constructor for the game class
     * @param state 2d array of integers representing the current state of the game
     * @param currentPlayer integer representig the current player of the game. 
     */
    public Game(int[][] state, int currentPlayer) {
        this.state = copyState(state);
        this.currentPlayer = currentPlayer;
    }

    /**
     * Copies the current state of the board
     * 
     * @param state 2d array of integers representing the current state of the game
     * @return 2d array of integers which is a copy of the current state
     */
    public static int[][] copyState(int[][] state) {
        int[][] newState = new int[state.length][state.length];
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++) {
                newState[i][j] = state[i][j];
            }
        }

        return newState;
    }

    // Return a list of available moves
    public abstract List<int[]> getSmartMoves();

    public abstract List<int[]> getSmartMoves(boolean enhanced, int rollouts);

    // Returns a new game object updated with new state
    public abstract Game getNextState(int[] move, boolean changePlayer);

    // Returns a new game object updated with new state
    public abstract Game getNextState(int[] move);

    // Checks to see if there are any new winners
    // returns 0 if there are no winners, otherwise -1 or 1
    public abstract int getWinner();

    // getState just returns the current state
    public int[][] getState() {
        return this.state;
    }

    public abstract Game copyGame();

    // Update the state of the current game state and flip the current player
    public abstract void play(int[] move);

    public abstract void printBoard();
}