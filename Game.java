import java.util.*;

public abstract class Game {
    public abstract List<int[]> returnAvailableMoves();

    public abstract Game play(int[] move);

    public abstract int getWinner();
}