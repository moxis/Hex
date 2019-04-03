import java.util.Map;
import java.util.HashMap;

class MonteCarlo {
    private Game game;
    private int exploreParam;

    private Map<int[][], Node> nodes = new HashMap<int[][], Node>();

    MonteCarlo(Game game, int exploreParam) {
        this.game = game;
        this.exploreParam = exploreParam;
    }

    public void search() {
        // TO BE IMPLEMENTED
    }

    public int[] returnBestMove() {
        return null;
    }

    // Phase 1
    public Node select() {
        return null;
    }

    // Phase 2
    public Node expand() {
        return null;
    }

    // Phase 3
    // In the simulation we can add heurestic so that the program blocks winning moves when encountered
    // So check if opponent is one move away from winning, if yes then block that move
    public int simulate() {
        return 0;
    }

    // Phase 4
    public int backpropagation() {
        return 0;
    }
}