class SimulationThread implements Runnable {
    private Thread t;
    private String threadName = "Thread";
    private int[][] state;
    private MonteCarlo tree;
    
    SimulationThread(int[][] state, MonteCarlo tree) {
       this.state = state;
       this.tree = tree;
    }
    
    public void run() {
       try {
            TreeNode node = tree.select(state);
            int winner = node.game.getWinner();

            if (!node.isLeaf() && winner == 0) {
                node = tree.expand(node);
                winner = tree.simulate(node);
            }
            // Update the other nodes
            tree.backpropagation(node, winner);
            MonteCarlo.i += 1;
       } catch (Exception e) {
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