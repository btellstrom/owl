// code by jph
package ch.ethz.idsc.sophus.app.jph;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.ethz.idsc.owl.bot.util.UserHome;
import ch.ethz.idsc.sophus.math.SmoothingKernel;
import ch.ethz.idsc.sophus.sym.SymLinkImage;
import ch.ethz.idsc.sophus.sym.SymLinkImages;

enum ReadmeImages {
  ;
  public static void main(String[] args) throws IOException {
    {
      SymLinkImage symLinkImage = SymLinkImages.smoothingKernel(SmoothingKernel.GAUSSIAN, 3);
      BufferedImage bufferedImage = symLinkImage.bufferedImage();
      ImageIO.write(bufferedImage, "png", UserHome.Pictures("gaussian3.png"));
    }
  }
}