// code by ob, jph
package ch.ethz.idsc.sophus.app.filter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.JSlider;

import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplays;
import ch.ethz.idsc.sophus.app.util.SpinnerLabel;
import ch.ethz.idsc.sophus.group.LieDifferences;
import ch.ethz.idsc.sophus.group.LieGroup;
import ch.ethz.idsc.sophus.group.RnExponential;
import ch.ethz.idsc.sophus.group.RnGroup;
import ch.ethz.idsc.subare.util.plot.ListPlot;
import ch.ethz.idsc.subare.util.plot.VisualSet;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.io.ResourceData;

/* package */ abstract class NavigableMapDatasetFilterDemo extends DatasetFilterDemo {
  private final JSlider jSlider = new JSlider(1, 999, 200);
  // ---
  protected Tensor _time = null;
  protected Tensor _state = null;
  protected Tensor _quality = null;
  protected final SpinnerLabel<String> spinnerLabelString = new SpinnerLabel<>();
  protected final SpinnerLabel<Integer> spinnerLabelLimit = new SpinnerLabel<>();

  protected void updateStateTime() {
    _time = Tensor.of(ResourceData.of("/dubilab/app/pose/" + spinnerLabelString.getValue() + ".csv").stream().limit(250).map(row -> row.Get(0)));
    _state = Tensor.of(ResourceData.of("/dubilab/app/pose/" + spinnerLabelString.getValue() + ".csv").stream().limit(250)
        .map(row -> row.extract(1, row.length()).map(geodesicDisplay()::project)));
    _quality = Tensor
        .of(ResourceData.of("/dubilab/app/pose/" + spinnerLabelString.getValue() + ".csv").stream().limit(250).map(row -> row.get(row.length() - 1)));
  }

  protected final NavigableMap<Scalar, Tensor> navigableMapStateTime() {
    NavigableMap<Scalar, Tensor> navigableMapStateTime = new TreeMap<>();
    for (int index = 0; index < _time.length(); ++index) {
      // remove all elements with quality below threshold
      if (Scalars.lessThan(qualityThreshold(), _quality.Get(index)))
        navigableMapStateTime.put(_time.Get(index), _state.get(index));
    }
    return navigableMapStateTime;
  }

  public NavigableMapDatasetFilterDemo() {
    super(GeodesicDisplays.CLOTH_SE2_R2);
    jSlider.setPreferredSize(new Dimension(500, 28));
    //
    timerFrame.geometricComponent.setModel2Pixel(StaticHelper.HANGAR_MODEL2PIXEL);
    // ---
    jToggleWait.setSelected(false);
    timerFrame.jToolBar.add(jToggleWait);
    // ---
    jToggleDiff.setSelected(true);
    timerFrame.jToolBar.add(jToggleDiff);
    // ---
    jToggleData.setSelected(true);
    timerFrame.jToolBar.add(jToggleData);
    // ---
    jToggleConv.setSelected(true);
    timerFrame.jToolBar.add(jToggleConv);
    {
      spinnerLabelString.setList(ResourceData.lines("/dubilab/app/pose/index.vector"));
      spinnerLabelString.addSpinnerListener(type -> updateStateTime());
      spinnerLabelString.setIndex(0);
      spinnerLabelString.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "data");
    }
    {
      spinnerLabelLimit.setList(Arrays.asList(10, 20, 50, 100, 250, 500, 1000, 2000, 5000));
      spinnerLabelLimit.setIndex(4);
      spinnerLabelLimit.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "limit");
      spinnerLabelLimit.addSpinnerListener(type -> updateStateTime());
    }
    timerFrame.jToolBar.addSeparator();
    // ---
    timerFrame.jToolBar.add(jToggleSymi);
    // ---
    timerFrame.jToolBar.add(jSlider);
  }

  private Scalar qualityThreshold() {
    return RationalScalar.of(jSlider.getValue(), 1000);
  }

  @Override
  protected Tensor control() {
    return _state;
  }

  /** @return */
  protected abstract String plotLabel();

  @Override
  protected void differences_render(Graphics2D graphics, GeodesicDisplay geodesicDisplay, Tensor refined) {
    LieGroup lieGroup = geodesicDisplay.lieGroup();
    if (Objects.nonNull(lieGroup)) {
      LieDifferences lieDifferences = new LieDifferences(lieGroup, geodesicDisplay.lieExponential());
      LieDifferences lieDifferencesTime = new LieDifferences(RnGroup.INSTANCE, RnExponential.INSTANCE);
      Tensor timeDifference = lieDifferencesTime.apply(Tensor.of(navigableMapStateTime().keySet().stream())).map(x -> x.reciprocal());
      Tensor speeds = timeDifference.pmul(lieDifferences.apply(refined));
      if (0 < speeds.length()) {
        int dimensions = speeds.get(0).length();
        VisualSet visualSet = new VisualSet();
        visualSet.setPlotLabel(plotLabel());
        visualSet.setAxesLabelX("sample no.");
        Tensor domain = Range.of(0, speeds.length());
        for (int index = 0; index < dimensions; ++index)
          visualSet.add(domain, speeds.get(Tensor.ALL, index)); // .setLabel("tangent velocity [m/s]")
        // visualSet.add(domain, speeds.get(Tensor.ALL, 1)).setLabel("side slip [m/s]");
        // visualSet.add(domain, speeds.get(Tensor.ALL, 2)).setLabel("rotational rate [rad/s]");
        JFreeChart jFreeChart = ListPlot.of(visualSet);
        jFreeChart.draw(graphics, new Rectangle2D.Double(0, 0, 80 + speeds.length(), 400));
      }
    }
  }
}
