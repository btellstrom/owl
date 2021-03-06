// code by jph
package ch.ethz.idsc.sophus.app.curve;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import ch.ethz.idsc.sophus.curve.BSpline1CurveSubdivision;
import ch.ethz.idsc.sophus.curve.BSpline2CurveSubdivision;
import ch.ethz.idsc.sophus.curve.BSpline3CurveSubdivision;
import ch.ethz.idsc.sophus.curve.BSpline4CurveSubdivision;
import ch.ethz.idsc.sophus.curve.BSpline5CurveSubdivision;
import ch.ethz.idsc.sophus.curve.CurveSubdivision;
import ch.ethz.idsc.sophus.curve.DualC2FourPointCurveSubdivision;
import ch.ethz.idsc.sophus.curve.FourPointCurveSubdivision;
import ch.ethz.idsc.sophus.curve.HormannSabinCurveSubdivision;
import ch.ethz.idsc.sophus.curve.LaneRiesenfeld3CurveSubdivision;
import ch.ethz.idsc.sophus.curve.LaneRiesenfeldCurveSubdivision;
import ch.ethz.idsc.sophus.curve.SixPointCurveSubdivision;
import ch.ethz.idsc.sophus.math.GeodesicInterface;
import ch.ethz.idsc.sophus.sym.SymGeodesic;
import ch.ethz.idsc.sophus.sym.SymLinkImage;
import ch.ethz.idsc.sophus.sym.SymScalar;
import ch.ethz.idsc.tensor.Tensor;

/* package */ enum SymMaskImages {
  BSPLINE1(BSpline1CurveSubdivision::new, 2, 0, 1), //
  BSPLINE2(BSpline2CurveSubdivision::new, 2, 0, 1), //
  BSPLINE3(BSpline3CurveSubdivision::new, 3, 1, 2), //
  BSPLINE3LR(LaneRiesenfeld3CurveSubdivision::new, 3, 1, 2), //
  BSPLINE4(BSpline4CurveSubdivision::of, 3, 2, 3), //
  BSPLINE4S2(BSpline4CurveSubdivision::split2, 3, 2, 3), //
  BSPLINE4S3(CurveSubdivisionHelper::split3, 3, 2, 3), //
  BSPLINE5(BSpline5CurveSubdivision::new, 4, 2, 3), //
  LR1(gi -> new LaneRiesenfeldCurveSubdivision(gi, 1), 2, 0, 1), //
  LR2(gi -> new LaneRiesenfeldCurveSubdivision(gi, 2), 3, 0, 1), //
  LR3(gi -> new LaneRiesenfeldCurveSubdivision(gi, 3), 3, 1, 2), //
  LR4(gi -> new LaneRiesenfeldCurveSubdivision(gi, 4), 5, 1, 2), //
  LR5(gi -> new LaneRiesenfeldCurveSubdivision(gi, 5), 5, 2, 3), //
  LR6(gi -> new LaneRiesenfeldCurveSubdivision(gi, 6), 5, 2, 3), //
  THREEPOINT(HormannSabinCurveSubdivision::of, 5, 1, 2), //
  FOURPOINT(FourPointCurveSubdivision::new, 6, 0, 3), //
  FOURPOINT2(CurveSubdivisionHelper::fps, 6, 0, 3), //
  C2CUBIC(DualC2FourPointCurveSubdivision::cubic, 6, 2, 3), //
  SIXPOINT(SixPointCurveSubdivision::new, 6, 0, 5), //
  ;
  // ---
  private final Function<GeodesicInterface, CurveSubdivision> function;
  private final int support;
  private final int index0;
  private final int index1;

  private SymMaskImages(Function<GeodesicInterface, CurveSubdivision> function, int support, int index0, int index1) {
    this.function = function;
    this.support = support;
    this.index0 = index0;
    this.index1 = index1;
  }

  private BufferedImage bufferedImage(int index) {
    CurveSubdivision curveSubdivision = function.apply(SymGeodesic.INSTANCE);
    Tensor vector = Tensor.of(IntStream.range(0, support).mapToObj(SymScalar::leaf));
    Tensor tensor = curveSubdivision.cyclic(vector);
    return new SymLinkImage((SymScalar) tensor.Get(index)).bufferedImage();
  }

  public BufferedImage image0() {
    return bufferedImage(index0);
  }

  public BufferedImage image1() {
    return bufferedImage(index1);
  }

  /***************************************************/
  public static Optional<SymMaskImages> get(String string) {
    try {
      return Optional.ofNullable(SymMaskImages.valueOf(string));
    } catch (Exception exception) {
      // ---
    }
    return Optional.empty();
  }
}
