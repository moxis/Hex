package v2;

import java.io.Serializable;
import java.util.*;

public abstract class Game {
    private int[][] state;
    int currentPlayer;

    Game(int[][] state, int currentPlayer) {
        this.state = state;
        this.currentPlayer = currentPlayer;
    }

    // Return a list of available moves
    public abstract List<int[]> getAvailableMoves();

    public abstract List<int[]> getAvailableMoves(boolean enhanced);

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

    // Update the state of the current game state and flip the current player
    public abstract void play(int[] move);
}