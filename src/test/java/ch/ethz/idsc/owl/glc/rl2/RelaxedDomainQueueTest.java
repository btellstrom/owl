// code by astoll
package ch.ethz.idsc.owl.glc.rl2;

import java.io.IOException;
import java.util.Random;

import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.math.VectorScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Serialization;
import ch.ethz.idsc.tensor.io.Timing;
import junit.framework.TestCase;

public class RelaxedDomainQueueTest extends TestCase {
  public void testAdd() {
    Tensor slacks = Tensors.vector(1, 1, 1);
    GlcNode node1 = GlcNode.of(null, null, VectorScalar.of(2, 1, 2), VectorScalar.of(0, 0, 0));
    GlcNode node2 = GlcNode.of(null, null, VectorScalar.of(1, 2, 1), VectorScalar.of(0, 0, 0));
    GlcNode node3 = GlcNode.of(null, null, VectorScalar.of(2, 2, 2), VectorScalar.of(0, 0, 0));
    GlcNode node4 = GlcNode.of(null, null, VectorScalar.of(2, 3, 2), VectorScalar.of(0, 0, 0));
    GlcNode node5 = GlcNode.of(null, null, VectorScalar.of(0, 2, 2), VectorScalar.of(0, 0, 0));
    RelaxedPriorityQueue rlQueue = RelaxedDomainQueue.singleton(node1, slacks);
    assertTrue(rlQueue.collection().contains(node1));
    rlQueue.add(node2);
    assertTrue(rlQueue.collection().contains(node1) && rlQueue.collection().contains(node2));
    rlQueue.add(node3);
    assertTrue(rlQueue.collection().contains(node1));
    assertTrue(rlQueue.collection().contains(node2));
    assertTrue(rlQueue.collection().contains(node3));
    rlQueue.add(node4);
    assertTrue(rlQueue.collection().contains(node1));
    assertTrue(rlQueue.collection().contains(node2));
    assertTrue(rlQueue.collection().contains(node3));
    assertFalse(rlQueue.collection().contains(node4));
    rlQueue.add(node5);
    assertTrue(rlQueue.collection().contains(node5));
    assertTrue(rlQueue.collection().contains(node2));
    assertFalse(rlQueue.collection().contains(node1) && rlQueue.collection().contains(node3) && rlQueue.collection().contains(node4));
  }

  public void testPeek() {
    Tensor slacks = Tensors.vector(1, 1, 1);
    GlcNode node1 = GlcNode.of(null, null, VectorScalar.of(2, 1, 2), VectorScalar.of(0, 0, 0));
    GlcNode node2 = GlcNode.of(null, null, VectorScalar.of(1, 2, 1), VectorScalar.of(0, 0, 0));
    GlcNode node3 = GlcNode.of(null, null, VectorScalar.of(2, 2, 2), VectorScalar.of(0, 0, 0));
    GlcNode node4 = GlcNode.of(null, null, VectorScalar.of(2, 3, 2), VectorScalar.of(0, 0, 0));
    GlcNode node5 = GlcNode.of(null, null, VectorScalar.of(0, 2, 2), VectorScalar.of(0, 0, 0));
    RelaxedPriorityQueue rlQueue = RelaxedDomainQueue.singleton(node1, slacks);
    assertTrue(rlQueue.collection().contains(node1));
    assertTrue(rlQueue.peekBest() == node1);
    assertTrue(rlQueue.collection().size() == 1);
    rlQueue.add(node2);
    assertTrue(rlQueue.peekBest() == node2);
    assertTrue(rlQueue.collection().size() == 2);
    rlQueue.add(node3);
    assertTrue(rlQueue.peekBest() == node2);
    assertTrue(rlQueue.collection().size() == 3);
    rlQueue.add(node4);
    assertTrue(rlQueue.peekBest() == node2);
    assertTrue(rlQueue.collection().size() == 3);
    rlQueue.add(node5);
    assertTrue(rlQueue.peekBest() == node5);
    assertTrue(rlQueue.collection().size() == 2);
  }

  public void testPoll() throws ClassNotFoundException, IOException {
    Tensor slacks = Tensors.vector(3, 3, 3);
    GlcNode node1 = GlcNode.of(null, null, VectorScalar.of(1, 1, 2), VectorScalar.of(0, 0, 0));
    GlcNode node2 = GlcNode.of(null, null, VectorScalar.of(1, 2, 1), VectorScalar.of(0, 0, 0));
    GlcNode node3 = GlcNode.of(null, null, VectorScalar.of(2, 2, 2), VectorScalar.of(0, 0, 0));
    GlcNode node4 = GlcNode.of(null, null, VectorScalar.of(2, 3, 2), VectorScalar.of(0, 0, 0));
    GlcNode node5 = GlcNode.of(null, null, VectorScalar.of(0, 2, 2), VectorScalar.of(0, 0, 0));
    RelaxedPriorityQueue rlQueue = RelaxedDomainQueue.singleton(node1, slacks);
    assertTrue(rlQueue.collection().size() == 1);
    rlQueue.add(node2);
    assertTrue(rlQueue.collection().size() == 2);
    rlQueue.add(node3);
    assertTrue(rlQueue.collection().size() == 3);
    rlQueue.add(node4);
    assertTrue(rlQueue.collection().size() == 4);
    rlQueue.add(node5);
    Serialization.copy(rlQueue);
    assertTrue(rlQueue.collection().size() == 5);
    assertTrue(rlQueue.pollBest() == node5);
    assertTrue(rlQueue.collection().size() == 4);
    assertTrue(rlQueue.pollBest() == node1);
    assertTrue(rlQueue.collection().size() == 3);
    assertTrue(rlQueue.pollBest() == node2);
    assertTrue(rlQueue.collection().size() == 2);
    assertTrue(rlQueue.pollBest() == node3);
    assertTrue(rlQueue.collection().size() == 1);
    assertTrue(rlQueue.pollBest() == node4);
    assertTrue(rlQueue.collection().isEmpty());
  }

  public void testEmpty() {
    Tensor slacks = Tensors.vector(3, 3, 3);
    RelaxedPriorityQueue rlQueue = RelaxedDomainQueue.empty(slacks);
    assertTrue(rlQueue.collection().isEmpty());
  }

  public void testSpeed() {
    Tensor slacks = Tensors.vector(1, 1, 1);
    Random random = new Random();
    Scalar costFromRoot = VectorScalar.of(Tensors.vectorDouble(random.doubles(3, 1, 2).toArray()));
    Scalar minCostToGoal = VectorScalar.of(0, 0, 0);
    GlcNode firstNode = GlcNode.of(null, null, costFromRoot, minCostToGoal);
    RelaxedPriorityQueue rlQueue = RelaxedDomainQueue.singleton(firstNode, slacks);
    for (int i = 0; i < 1000; ++i) {
      costFromRoot = VectorScalar.of(Tensors.vectorDouble(random.doubles(3, 1, 2).toArray()));
      minCostToGoal = VectorScalar.of(0, 0, 0);
      GlcNode node = GlcNode.of(null, null, costFromRoot, minCostToGoal);
      rlQueue.add(node);
    }
    Timing timing = Timing.started();
    rlQueue.pollBest();
    System.out.println(timing.seconds());
  }
}
