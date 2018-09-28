// code by jph
package ch.ethz.idsc.owl.math;

import ch.ethz.idsc.tensor.ExactScalarQ;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Series;
import ch.ethz.idsc.tensor.alg.Sort;
import ch.ethz.idsc.tensor.alg.UnitVector;
import ch.ethz.idsc.tensor.alg.VectorQ;
import ch.ethz.idsc.tensor.mat.HilbertMatrix;
import ch.ethz.idsc.tensor.pdf.Distribution;
import ch.ethz.idsc.tensor.pdf.NormalDistribution;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import ch.ethz.idsc.tensor.sca.Chop;
import junit.framework.TestCase;

public class RootsTest extends TestCase {
  public void testConstantUniform() {
    Tensor roots = Roots.of(Tensors.vector(2));
    assertTrue(Tensors.isEmpty(roots));
  }

  public void testZeros() {
    Tensor roots = Roots.of(Tensors.vector(0, 0, 1, 0));
    assertEquals(roots, Array.zeros(2));
  }

  public void testUnitVector() {
    for (int length = 1; length < 10; ++length) {
      Tensor coeffs = UnitVector.of(length, length - 1);
      assertEquals(Roots.of(coeffs), Array.zeros(length - 1));
    }
  }

  public void testUnitVectorPlus() {
    for (int length = 1; length < 10; ++length) {
      Tensor coeffs = UnitVector.of(length + 3, length - 1);
      assertEquals(Roots.of(coeffs), Array.zeros(length - 1));
    }
  }

  public void testZerosQuantity() {
    Tensor roots = Roots.of(Tensors.fromString("{0, 0, 1[m^-2],0[m^-3]}"));
    assertEquals(roots, Tensors.fromString("{0[m^2], 0[m^2]}"));
  }

  public void testLinearUniform() {
    Distribution distribution = UniformDistribution.of(-10, 10);
    for (int index = 0; index < 200; ++index) {
      Tensor coeffs = RandomVariate.of(distribution, 2);
      if (Scalars.nonZero(coeffs.Get(1))) {
        Tensor roots = Roots.of(coeffs);
        VectorQ.requireLength(roots, 1);
        Tensor check = roots.map(Series.of(coeffs));
        assertTrue(Chop._12.allZero(check));
      } else
        System.out.println("skip " + coeffs);
    }
  }

  public void testQuadraticUniform() {
    Distribution distribution = UniformDistribution.of(-5, 5);
    for (int index = 0; index < 200; ++index) {
      Tensor coeffs = RandomVariate.of(distribution, 3);
      if (Scalars.nonZero(coeffs.Get(2))) {
        Tensor roots = Roots.of(coeffs);
        VectorQ.requireLength(roots, 2);
        Tensor check = roots.map(Series.of(coeffs));
        assertTrue(Chop._10.allZero(check));
      } else
        System.out.println("skip " + coeffs);
    }
  }

  public void testQuadraticNormal() {
    Distribution distribution = NormalDistribution.of(0, .3);
    for (int index = 0; index < 200; ++index) {
      Tensor coeffs = RandomVariate.of(distribution, 3);
      Tensor roots = Roots.of(coeffs);
      Tensor check = roots.map(Series.of(coeffs));
      assertTrue(Chop._12.allZero(check));
    }
  }

  public void testQuadraticQuantity() {
    Tensor coeffs = Tensors.fromString("{21, - 10 [s^-1], +1 [s^-2], 0, 0, 0}");
    Tensor roots = Roots.of(coeffs);
    assertEquals(roots, Tensors.fromString("{3[s], 7[s]}"));
    assertTrue(ExactScalarQ.all(roots));
  }

  public void testQuadraticComplexQuantity() {
    Tensor coeffs = Tensors.fromString("{1, 0 [s^-1], 1 [s^-2]}");
    Tensor roots = Roots.of(coeffs);
    assertEquals(roots, Tensors.fromString("{-I[s], I[s]}"));
    assertTrue(ExactScalarQ.all(roots));
  }

  public void testPseudoCubicQuantity() {
    Tensor coeffs = Tensors.fromString("{0, 21, - 10 [s^-1], +1 [s^-2], 0, 0, 0}");
    Tensor roots = Roots.of(coeffs);
    assertEquals(Sort.of(roots), Tensors.fromString("{0[s], 3[s], 7[s]}"));
    assertTrue(ExactScalarQ.all(roots));
  }

  public void testPseudoQuarticQuantity() {
    Tensor coeffs = Tensors.fromString("{0, 0, 21, - 10 [s^-1], +1 [s^-2], 0, 0, 0}");
    Tensor roots = Roots.of(coeffs);
    assertEquals(Sort.of(roots), Tensors.fromString("{0[s], 0[s], 3[s], 7[s]}"));
    assertTrue(ExactScalarQ.all(roots));
  }

  public void testScalarFail() {
    try {
      Roots.of(RealScalar.ONE);
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testEmptyFail() {
    try {
      Roots.of(Tensors.empty());
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testOnes() {
    Tensor coeffs = Tensors.vector(0);
    try {
      Roots.of(coeffs);
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testConstantZeroFail() {
    try {
      Roots.of(Tensors.vector(0));
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testZerosFail() {
    for (int length = 0; length < 10; ++length)
      try {
        Roots.of(Array.zeros(length));
        assertTrue(false);
      } catch (Exception exception) {
        // ---
      }
  }

  public void testMatrixFail() {
    try {
      Roots.of(HilbertMatrix.of(2, 3));
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testNotImplemented() {
    try {
      Roots.of(Tensors.vector(1, 2, 3, 4, 5, 6));
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }
}