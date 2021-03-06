// code by ob
package ch.ethz.idsc.sophus.group;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Chop;
import junit.framework.TestCase;

public class StGroupElementTest extends TestCase {
  public void testSt1Inverse() {
    Tensor p = Tensors.vector(3, 6);
    Tensor id = Tensors.vector(1, 0);
    StGroupElement pE = new StGroupElement(p);
    assertEquals(pE.inverse().combine(p), id);
  }

  public void testSt1Combine() {
    Tensor p = Tensors.vector(3, 6);
    StGroupElement pE = new StGroupElement(p);
    Tensor q = Tensors.vector(2, 8);
    assertEquals(pE.combine(q), Tensors.vector(2 * 3, 3 * 8 + 6));
  }

  public void testInverse() {
    for (int count = 0; count < 100; ++count) {
      Scalar lambda = RealScalar.of(Math.random() + 0.001);
      Tensor t = Tensors.vector(Math.random(), 32 * Math.random(), -Math.random(), -17 * Math.random());
      Tensor p = Tensors.of(lambda, t);
      Tensor id = Tensors.of(RealScalar.ONE, Tensors.vector(0, 0, 0, 0));
      StGroupElement pE = new StGroupElement(p);
      Chop._11.requireClose(pE.inverse().combine(p), id);
    }
  }

  public void testCombine() {
    Scalar lambda = RealScalar.of(2);
    Tensor t = Tensors.vector(0, 1, -2);
    Tensor p = Tensors.of(lambda, t);
    StGroupElement pE = new StGroupElement(p);
    Scalar lambda2 = RealScalar.of(2);
    Tensor t2 = Tensors.vector(2, 3, 4);
    Tensor q = Tensors.of(lambda2, t2);
    assertEquals(pE.combine(q), Tensors.of(RealScalar.of(4), Tensors.vector(4, 7, 6)));
  }

  // checks that lambda is required to be positive
  public void testLambdaNonPositiveFail() {
    try {
      new StGroupElement(Tensors.vector(0, 5));
      fail();
    } catch (Exception exception) {
      // ---
    }
    try {
      new StGroupElement(Tensors.vector(-1, 5));
      fail();
    } catch (Exception exception) {
      // ---
    }
  }
}