import java.util.*;
import game.*;

public class TreeNode {
    // Classic tree node properties
    TreeNode parent = null;
    Map<Integer, TreeNode> children = new HashMap<Integer, TreeNode>();

    int numberOfChildren;

    // To calculate UCB1
    // Only these stats and available moves will be saved so that we won't have to recompute them
    int numRollouts = 0;
    int numWins = 0;

    int numRaveRollouts = 0;
    int numRaveWins = 0;

    Game game;
    int[] action;

    static double explorationParam = 0;
    static double raveConstant = 24000.0;
    List<int[]> availableMoves;

    TreeNode(TreeNode parent, Game game, int[] action, List<int[]> availableMoves) {
        this.parent = parent;
        this.action = action;
        this.game = game;
        this.availableMoves = availableMoves;

        for(int[] move : this.availableMoves) {
            children.put(hash(move), null);
        }

        this.numberOfChildren = children.size();
    }

    // Need this method because arrays can't be used reliably as keys
    public static Integer hash(int[] arr) {
        // HASHCODES ARE CONFLICTED AHH
        return Arrays.hashCode(arr);
    }

    public List<int[]> getAllMoves() {
        return this.availableMoves;
    }

    public double getUCB1(boolean rave) {
        if(rave) {
            double alpha = Math.max(0, (raveConstant - (double) this.numRollouts) / raveConstant);
            double score = (((double) this.numWins) * (1-alpha) / this.numRollouts) + (((double) this.numRaveWins) * alpha / this.numRaveRollouts); 
            if (!Double.isNaN(score)) {
                return score;
            }
        }

        return ((double) numWins) / numRollouts + Math.sqrt(explorationParam * Math.log(parent.numRollouts) / numRollouts);
    }

    public boolean isLeaf() {
        return this.numberOfChildren == 0;
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