import java.util.*;

class SimulationThread implements Runnable {
   static List<int[]> emptyArray = new ArrayList<int[]>();

   private Thread t;
   private String threadName = "Thread";
   private int[][] state;
   private MonteCarlo tree;
   
   SimulationThread(int[][] state, MonteCarlo tree) {
      this.state = state;
      this.tree = tree;
   }
   
   @SuppressWarnings("unchecked")
   public void run() {
      try {
         TreeNode node = tree.select(state);
         int winner = node.game.getWinner();
         Object[] results = new Object[] {winner, emptyArray, emptyArray};

         if (!node.isLeaf() && winner == 0) {
               node = tree.expand(node);
               results = tree.simulate(node);
               winner = (int) results[0];
         }
         // Update the other nodes
         
         tree.backpropagation(node, winner, (List<int[]>) results[1], (List<int[]>) results[2]);
         MonteCarlo.i += 1;
      } catch (Exception e) {
         // System.out.println("Multithread");
         // e.printStackTrace();
      }
   }
   
   public void start () {
      //System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread(this, threadName);
         t.start();
      }
   }
 }