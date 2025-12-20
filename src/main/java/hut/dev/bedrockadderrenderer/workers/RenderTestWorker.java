package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.blending.BlendingModes;
import com.loohp.blockmodelrenderer.render.Hexahedron;
import com.loohp.blockmodelrenderer.render.Model;
import com.loohp.blockmodelrenderer.render.Point3D;
import com.loohp.blockmodelrenderer.render.Vector;
import com.loohp.blockmodelrenderer.utils.TaskCompletion;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RenderTestWorker
{
    private final Logger logger;

    public RenderTestWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void renderCookieBlockCube(Path cookieBlockTexturePath, int outputImageSize, Path outputPngPath) throws Exception
    {
        System.setProperty("java.awt.headless", "true");

        if (!Files.exists(cookieBlockTexturePath))
        {
            throw new IllegalStateException("Texture not found: " + cookieBlockTexturePath);
        }

        logger.info("Loading texture: " + cookieBlockTexturePath);
        BufferedImage cookieTextureImage = ImageIO.read(cookieBlockTexturePath.toFile());
        if (cookieTextureImage == null)
        {
            throw new IllegalStateException("ImageIO failed to read texture: " + cookieBlockTexturePath);
        }

        // BlockModelRenderer expects 6 images (one per face). For a test cube, reuse the same texture for all faces.
        BufferedImage[] cubeFaceImages = new BufferedImage[]
                {
                        cookieTextureImage,
                        cookieTextureImage,
                        cookieTextureImage,
                        cookieTextureImage,
                        cookieTextureImage,
                        cookieTextureImage
                };

        // Use Minecraft model units (0..16).
        Point3D cubeCornerMin = new Point3D(0.0, 0.0, 0.0);
        Point3D cubeCornerMax = new Point3D(16.0, 16.0, 16.0);

        Hexahedron cubeHexahedron = Hexahedron.fromCorners(cubeCornerMin, cubeCornerMax, cubeFaceImages);
        Model cubeModel = new Model(cubeHexahedron);

        // Center model around origin then rotate like a typical GUI-ish view.
        cubeModel.translate(-8.0, -8.0, -8.0);
        cubeModel.rotate(30.0, 225.0, 0.0, false);

        // Lighting (directional-ish)
        cubeModel.updateLighting(new Vector(-0.7, 1.0, -0.7), 1.25, 0.75);

        // Output image MUST be DataBufferInt compatible -> TYPE_INT_ARGB is good.
        BufferedImage outputImage = new BufferedImage(outputImageSize, outputImageSize, BufferedImage.TYPE_INT_ARGB);

        // AffineTransform: center on canvas, scale up.
        double scaleFactor = outputImageSize / 32.0; // good starting point for a 16x16 cube
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(outputImageSize / 2.0, outputImageSize / 2.0);
        affineTransform.scale(scaleFactor, scaleFactor);

        ExecutorService executorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        try
        {
            logger.info("Rendering cube to: " + outputPngPath);

            TaskCompletion taskCompletion = cubeModel.render(
                    outputImage,
                    true,
                    affineTransform,
                    BlendingModes.NORMAL,
                    executorService
            );

            taskCompletion.join();

            Files.createDirectories(outputPngPath.getParent());
            ImageIO.write(outputImage, "PNG", outputPngPath.toFile());

            logger.info("Render finished. Wrote: " + outputPngPath);
        }
        finally
        {
            executorService.shutdownNow();
        }
    }
}