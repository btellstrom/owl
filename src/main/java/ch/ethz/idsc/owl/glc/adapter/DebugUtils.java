// code by jl
package ch.ethz.idsc.owl.glc.adapter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.owl.data.GlobalAssert;
import ch.ethz.idsc.owl.data.tree.Nodes;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.GlcNodes;
import ch.ethz.idsc.owl.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.tensor.Scalars;

public enum DebugUtils {
  ;
  // ---
  public static void nodeAmountCompare(GlcNode best, int size) {
    final GlcNode root = Nodes.rootFrom(best);
    if (size != Nodes.ofSubtree(root).size()) {
      System.out.println("****NODE CHECK****");
      System.out.println("Nodes in DomainMap: " + size);
      System.out.println("Nodes in SubTree from Node: " + Nodes.ofSubtree(best).size());
      throw new RuntimeException();
    }
    System.out.println("Nodes in DomainMap: " + size);
    System.out.println("Nodes in SubTree from Node: " + Nodes.ofSubtree(best).size());
  }

  public static void connectivityCheck(Collection<GlcNode> treeCollection) {
    Iterator<GlcNode> iterator = treeCollection.iterator();
    while (iterator.hasNext()) {
      GlcNode node = iterator.next();
      if (!node.isRoot())
        GlobalAssert.that(node.parent().children().contains(node));
    }
  }

  public static void assertAllLeaf(Collection<GlcNode> collection) {
    boolean allLeaf = collection.stream().allMatch(GlcNode::isLeaf);
    if (!allLeaf)
      throw new RuntimeException("Not all elements in global queue are leafs!");
  }

  /** Checks if the Cost and the Heuristic along the found trajectory are consistent
   * 
   * @param trajectoryPlanner */
  public static final void heuristicConsistencyCheck(TrajectoryPlanner trajectoryPlanner) {
    Optional<GlcNode> finalNode = trajectoryPlanner.getBest();
    if (!finalNode.isPresent()) {
      System.out.println("No Final GoalNode, therefore no ConsistencyCheck");
      return;
    }
    List<GlcNode> trajectory = Nodes.listFromRoot(finalNode.get());
    // omit last Node, since last node may lie outside of goal region, as Trajectory to it was in
    connectivityCheck(trajectory);
    for (GlcNode current : trajectory) {
      GlcNode parent = current.parent();
      if (Scalars.lessEquals(current.costFromRoot(), parent.costFromRoot())) {
        System.err.println("At time " + current.stateTime().time() + " cost from root decreased from " + //
            parent.costFromRoot() + " to " + current.costFromRoot());
        StateTimeTrajectories.print(GlcNodes.getPathFromRootTo(finalNode.get()));
        throw new RuntimeException();
      }
      if (Scalars.lessEquals(current.merit(), parent.merit())) {
        System.err.println("At time " + current.stateTime().time() + " merit decreased from  " + //
            parent.merit() + " to " + current.merit());
        StateTimeTrajectories.print(GlcNodes.getPathFromRootTo(finalNode.get()));
        throw new RuntimeException();
      }
      // monotonously increasing merit means, that delta(Cost) >= delta(CostToGo)
      // as: Cost(Goal)== Merit(Goal) >= (Cost(Node) + CostToGo(Node)) = Merit (Node)
    }
  }
}
