import java.util.*;

public abstract class AIEngine {
    Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    public List<int[]> getAvailableMoves() {
        return this.getAvailableMoves(true, 9000);
    }

    public abstract List<int[]> getAvailableMoves(boolean enhanced, int rollouts);
}