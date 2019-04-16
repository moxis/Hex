import java.util.*;

public class TreeNode {
    // Classic tree node properties
    TreeNode parent = null;
    Map<Integer, TreeNode> children = new HashMap<Integer, TreeNode>();

    // To calculate UCB1
    int numRollouts = 0;
    int numWins = 0;

    Game game;
    int[] action;

    double explorationParam = 2;
    List<int[]> availableMoves;

    TreeNode(TreeNode parent, Game game, int[] action, List<int[]> availableMoves) {
        this.parent = parent;
        this.action = action;
        this.game = game;
        this.availableMoves = availableMoves;

        for(int[] move : this.availableMoves) {
            children.put(hash(move), null);
        }
    }

    // Need this method because arrays can't be used reliably as keys
    public static Integer hash(int[] arr) {
        // HASHCODES ARE CONFLICTED AHH
        return Arrays.hashCode(arr);
    }

    public List<int[]> getAllMoves() {
        return this.availableMoves;
    }

    public double getUCB1() {
        return ((double) numWins) / numRollouts + Math.sqrt(explorationParam * Math.log(parent.numRollouts) / numRollouts);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public boolean isFullyExpanded() {
        for(TreeNode value : children.values()) {
            if (value == null) {
                return false;
            }
        }

        return true;
    }

    public List<int[]> getUnexploredMoves() {
        List<int[]> unexploredMoves = new ArrayList<int[]>();
        for(int[] key : this.availableMoves) {
            if (children.get(hash(key)) == null) {
                unexploredMoves.add(key);
            }
        }

        return unexploredMoves;
    }
}