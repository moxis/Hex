import java.util.Map;
import java.util.HashMap;

class Node {
    private int[] play;
    private int[][] state;

    private int numPlayouts;
    private int numVictories;

    private Node parent;
    private Map<int[], Node> children = new HashMap<int[], Node>();

    Node(int[] play, int[][] state, Node parent, int[][] possibleMoves) {
        this.play = play;
        this.state = state;
        this.numPlayouts = 0;
        this.numVictories = 0;

        this.parent = parent;
        for(int[] move : possibleMoves) {
            children.put(move, null);
        }
    }

    public Node getChildren(int[] play) {
        // returns a child node with the corresponsing play
        return null;
    }

    public Node expansion() {
        return null;
    }

    public boolean isNodeFullyExpanded() {
        return false;
    }

    public int[][] getPossibleMoves() {
        return null;
    }

    public double getUCB1() {
        return 0;
    }
 }