import java.util.*;
import game.*;

public class TreeNode {
    // Classic tree node properties
    TreeNode parent = null;
    Map<Integer, TreeNode> children = new HashMap<Integer, TreeNode>();

    int numberOfChildren;

    // To calculate UCB1
    // Only these stats and available moves will be saved so that we won't have to
    // recompute them
    int numRollouts = 0;
    int numWins = 0;

    int numRaveRollouts = 0;
    int numRaveWins = 0;

    Game game;
    int[] action;

    static double explorationParam = 0;
    static double raveConstant = 24000.0;
    List<int[]> availableMoves;

    /**
     * Constuctor for the tree node class
     * 
     * @param parent         Treenode object representing the parent node of the
     *                       node being created
     * @param game           Game object representing he current state of the game
     * @param action         Integer array representing the list of actions that
     *                       could potentially occur
     * @param availableMoves List of arrays of integers representing the available
     *                       moves that could be made
     */
    TreeNode(TreeNode parent, Game game, int[] action, List<int[]> availableMoves) {
        this.parent = parent;
        this.action = action;
        this.game = game;
        this.availableMoves = availableMoves;

        for (int[] move : this.availableMoves) {
            children.put(hash(move), null);
        }

        this.numberOfChildren = children.size();
    }

    // Need this method because arrays can't be used reliably as keys
    public static Integer hash(int[] arr) {
        // HASHCODES ARE CONFLICTED AHH
        return Arrays.hashCode(arr);
    }

    /**
     * Finds all the moves that are reasonable to be made
     * 
     * @return List of arrays of integers representing the possible moves
     */
    public List<int[]> getAllMoves() {
        return this.availableMoves;
    }

    /**
     * 
     * @param rave
     * @return
     */
    public double getUCB1(boolean rave) {
        if (rave) {
            double alpha = Math.max(0, (raveConstant - (double) this.numRollouts) / raveConstant);
            double score = (((double) this.numWins) * (1 - alpha) / this.numRollouts)
                    + (((double) this.numRaveWins) * alpha / this.numRaveRollouts);
            if (!Double.isNaN(score)) {
                return score;
            }
        }

        return ((double) numWins) / numRollouts
                + Math.sqrt(explorationParam * Math.log(parent.numRollouts) / numRollouts);
    }

    /**
     * Checks to see if the node is the final branch of the node (aka the leaf).
     * 
     * @return Boolean saying if the current node is final
     */
    public boolean isLeaf() {
        return this.numberOfChildren == 0;
    }

    /**
     * Checks to see if all nodes have been checked in the tree
     * 
     * @return Boolean saying if the tree is fully explored
     */
    public boolean isFullyExpanded() {
        for (TreeNode value : children.values()) {
            if (value == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds any moves that have yet to be checked
     * 
     * @return List of arrays of integers that represents the possible moves.
     */
    public List<int[]> getUnexploredMoves() {
        List<int[]> unexploredMoves = new ArrayList<int[]>();
        for (int[] key : this.availableMoves) {
            if (children.get(hash(key)) == null) {
                unexploredMoves.add(key);
            }
        }
        return unexploredMoves;
    }
}