// code by jph
package ch.ethz.idsc.sophus.app.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;

import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.VectorQ;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.N;

/** class is used in other projects outside of owl */
public abstract class ControlPointsDemo extends GeodesicDisplayDemo {
  private static final Scalar THRESHOLD = RealScalar.of(0.2);
  /** control points */
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(255, 128, 128, 64), new Color(255, 128, 128, 255));
  /** refined points */
  private static final PointsRender POINTS_RENDER_1 = //
      new PointsRender(new Color(160, 160, 160, 128 + 64), Color.BLACK);
  // ---
  private final JButton jButton = new JButton("clear");
  // ---
  private Tensor control = Tensors.empty();
  private Tensor mouse = Array.zeros(3);
  private Integer min_index = null;
  // ---
  private final RenderInterface renderInterface = new RenderInterface() {
    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      mouse = geometricLayer.getMouseSe2State();
      if (Objects.nonNull(min_index))
        control.set(mouse, min_index);
      if (Objects.isNull(min_index)) {
        GeodesicDisplay geodesicDisplay = geodesicDisplay();
        Optional<Integer> optional = closest();
        graphics.setColor(optional.isPresent() ? Color.ORANGE : Color.GREEN);
        geometricLayer.pushMatrix(geodesicDisplay.matrixLift(geodesicDisplay.project(mouse)));
        graphics.fill(geometricLayer.toPath2D(getControlPointShape()));
        geometricLayer.popMatrix();
      }
    }
  };
  private final ActionListener actionListener = actionEvent -> {
    min_index = null;
    control = Tensors.empty();
  };

  public ControlPointsDemo(boolean clearButton, List<GeodesicDisplay> list) {
    super(list);
    if (clearButton) {
      jButton.addActionListener(actionListener);
      timerFrame.jToolBar.add(jButton);
    }
    timerFrame.geometricComponent.jComponent.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == 1) {
          if (Objects.isNull(min_index)) {
            min_index = closest().orElse(null);
            if (Objects.isNull(min_index)) {
              min_index = control.length();
              control.append(mouse);
            }
          } else {
            min_index = null;
            released();
          }
        }
      }
    });
    timerFrame.geometricComponent.addRenderInterface(renderInterface);
  }

  public Tensor getControlPointShape() {
    return geodesicDisplay().shape();
  }

  /** function is called when mouse is released */
  public void released() {
    // ---
  }

  private Optional<Integer> closest() {
    Scalar cmp = THRESHOLD;
    int index = 0;
    Integer min_index = null;
    for (Tensor point : control) {
      Scalar distance = Norm._2.between(point.extract(0, 2), mouse.extract(0, 2));
      if (Scalars.lessThan(distance, cmp)) {
        cmp = distance;
        min_index = index;
      }
      ++index;
    }
    return Optional.ofNullable(min_index);
  }

  public final void addButtonDubins() {
    JButton jButton = new JButton("dubins");
    jButton.setToolTipText("project control points to dubins path");
    jButton.addActionListener(actionEvent -> setControlPointsSe2(DubinsGenerator.project(control)));
    timerFrame.jToolBar.add(jButton);
  }

  /** @param control points as matrix of dimensions N x 3 */
  public final void setControlPointsSe2(Tensor control) {
    this.control = Tensor.of(control.stream() //
        .map(row -> VectorQ.requireLength(row, 3).map(Tensor::copy)));
  }

  /** @return control points as matrix of dimensions N x 3 */
  public final Tensor getControlPointsSe2() {
    return control.unmodifiable();
  }

  /** @return control points for selected {@link GeodesicDisplay} */
  public final Tensor getGeodesicControlPoints() {
    return Tensor.of(control.stream().map(geodesicDisplay()::project).map(N.DOUBLE::of));
  }

  protected final void renderControlPoints(GeometricLayer geometricLayer, Graphics2D graphics) {
    POINTS_RENDER_0.new Show(geodesicDisplay(), getControlPointShape(), getGeodesicControlPoints()).render(geometricLayer, graphics);
  }

  protected final void renderPoints( //
      GeodesicDisplay geodesicDisplay, Tensor points, //
      GeometricLayer geometricLayer, Graphics2D graphics) {
    POINTS_RENDER_1.new Show(geodesicDisplay, getControlPointShape(), points).render(geometricLayer, graphics);
  }
}
