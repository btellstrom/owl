// code by astoll, ynager
package ch.ethz.idsc.owl.glc.rl2;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.ethz.idsc.owl.data.tree.Nodes;
import ch.ethz.idsc.owl.data.tree.StateCostNode;
import ch.ethz.idsc.owl.glc.core.ControlsIntegrator;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.GoalInterface;
import ch.ethz.idsc.owl.glc.core.PlannerConstraint;
import ch.ethz.idsc.owl.glc.core.StateTimeRaster;
import ch.ethz.idsc.owl.math.flow.Flow;
import ch.ethz.idsc.owl.math.state.StateIntegrator;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.tensor.Tensor;

public class StandardRelaxedLexicographicPlanner extends RelaxedTrajectoryPlanner {
  private final StateIntegrator stateIntegrator;
  private final PlannerConstraint plannerConstraint;
  private final GoalInterface goalInterface;
  private transient final ControlsIntegrator controlsIntegrator;

  public StandardRelaxedLexicographicPlanner(//
      StateTimeRaster stateTimeRaster, //
      StateIntegrator stateIntegrator, //
      Collection<Flow> controls, //
      PlannerConstraint plannerConstraints, //
      GoalInterface goalInterface, //
      Tensor slacks) {
    super(stateTimeRaster, goalInterface, slacks);
    this.stateIntegrator = stateIntegrator;
    this.plannerConstraint = Objects.requireNonNull(plannerConstraints);
    this.goalInterface = goalInterface;
    controlsIntegrator = new ControlsIntegrator( //
        stateIntegrator, //
        () -> controls.stream().parallel(), //
        goalInterface);
  }

  @Override // from ExpandInterface
  public void expand(final GlcNode node) {
    Map<GlcNode, List<StateTime>> connectors = controlsIntegrator.from(node);
    // ---
    for (GlcNode next : connectors.keySet()) { // <- order of keys is non-deterministic
      // TODO ANDRE make "deterministic"
      final Tensor domainKey = stateTimeRaster.convertToKey(next.stateTime());
      final List<StateTime> trajectory = connectors.get(next);
      // check if planner constraints are satisfied otherwise discard next
      if (plannerConstraint.isSatisfied(node, trajectory, next.flow())) {
        // potentially add next to domainMap and save eventual discarded nodes
        Collection<GlcNode> discardedNodes = addToDomainMap(domainKey, next);
        // add next to global queue if accept by domainQueue and insert edge
        if (!discardedNodes.contains(next)) {
          addToGlobalQueue(next);
          node.insertEdgeTo(next);
          // check if trajectory went through goal region and to goalDomainQueue if so
          if (goalInterface.firstMember(trajectory).isPresent()) // GOAL check
            offerDestination(next, trajectory);
        }
        if (!discardedNodes.isEmpty() && !discardedNodes.contains(next)) {
          // TODO ANDRE check if sufficient, criteria here: not next and not empty
          // remove all discarded nodes in GlobalQueue from it
          this.removeChildren(discardedNodes);
        }
      }
    }
    // TODO ANDRE check if close to other merits see StaticHelper
  }

  private void removeChildren(Collection<? extends StateCostNode> discardedNodes) {
    // TODO TEST
    Iterator<? extends StateCostNode> iteratorDiscarded = discardedNodes.iterator();
    while (iteratorDiscarded.hasNext()) {
      GlcNode toDiscard = (GlcNode) iteratorDiscarded.next();
      if (toDiscard.isLeaf()) {
        // remove from globalQueue
        final Tensor domainKey = stateTimeRaster.convertToKey(toDiscard.stateTime());
        getGlobalQueue().remove(toDiscard);
        removeFromDomainQueue(domainKey, toDiscard);
        if (!toDiscard.isRoot())
          Nodes.disjoinChild(toDiscard);
        // TODO Andre remove from Discarded
      }
      removeChildren(toDiscard.children());
    }
  }

  @Override // from TrajectoryPlanner
  public final StateIntegrator getStateIntegrator() {
    return stateIntegrator;
  }
}
