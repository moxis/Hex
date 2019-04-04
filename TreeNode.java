import java.util.*;

public class TreeNode {
    // Classic tree node properties
    TreeNode parent = null;
    Map<int[], TreeNode> children = new HashMap<int[], TreeNode>();

    // To calculate UCB1
    int numRollouts = 0;
    int numWins = 0;

    Game game;
    int[] action;

    double explorationParam = 2;

    TreeNode(TreeNode parent, Game game, int[] action, List<int[]> availableMoves) {
        this.parent = parent;
        this.action = action;
        this.game = game;

        for(int[] move : availableMoves) {
            children.put(move, null);
        }
    }

    public Set<int[]> getAllMoves() {
        return children.keySet();
    }

    public double getUCB1() {
        return numWins / numRollouts + Math.sqrt(explorationParam * Math.log(parent.numRollouts) / numRollouts);
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
        for(int[] key : children.keySet()) {
            if (children.get(key) == null) {
                unexploredMoves.add(key);
            }
        }

        return unexploredMoves;
    }
}