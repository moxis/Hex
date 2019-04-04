import java.util.*;

public abstract class Game {
    int currentPlayer = 1;

    public abstract List<int[]> getAvailableActions();

    public abstract Game getNextState(int[] move);

    public abstract int getWinner();

    public abstract int[][] getState();
}