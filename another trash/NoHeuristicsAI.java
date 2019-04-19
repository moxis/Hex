import java.util.*;

public class NoHeuristicsAI extends Hex {
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

        return possibleMoves;
    }
}