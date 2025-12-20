package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.blending.BlendingModes;
import com.loohp.blockmodelrenderer.render.Face;
import com.loohp.blockmodelrenderer.render.Hexahedron;
import com.loohp.blockmodelrenderer.render.Model;
import com.loohp.blockmodelrenderer.render.Point3D;
import com.loohp.blockmodelrenderer.render.Vector;
import com.loohp.blockmodelrenderer.utils.TaskCompletion;
import org.slf4j.Logger;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ModelRenderWorker
{
    private final Logger logger;

    public ModelRenderWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void renderModelJsonFile(Path modelJsonFilePath, Path cacheAssetsDirectoryPath, int outputImageSize, Path outputPngFilePath) throws Exception
    {
        System.setProperty("java.awt.headless", "true");

        if (!Files.exists(modelJsonFilePath))
        {
            throw new IllegalStateException("Model JSON not found: " + modelJsonFilePath);
        }

        MinecraftModelJson modelJson = new ModelJsonReadingWorker(logger).readModelJson(modelJsonFilePath);

        Model builtModel = new ModelBuildingWorker(logger).buildModelFromMinecraftJson(modelJson, cacheAssetsDirectoryPath);

        new ModelTransformApplyingWorker(logger).applyGuiDisplayTransform(builtModel, modelJson);

        builtModel.updateLighting(new Vector(-0.7, 1.0, -0.7), 1.25, 0.75);

        new ModelFittingWorker(logger).centerModelOnOriginUsingXYBounds(builtModel);

        int internalImageSize = outputImageSize * 4;
        BufferedImage internalImage = new BufferedImage(internalImageSize, internalImageSize, BufferedImage.TYPE_INT_ARGB);

        double margin = 0.10;
        double maxDimension = new ModelFittingWorker(logger).getMaxXYDimension(builtModel);
        if (maxDimension <= 0.00001)
        {
            maxDimension = 16.0;
        }

        double scaleFactor = (internalImageSize * (1.0 - (margin * 2.0))) / maxDimension;

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(internalImageSize / 2.0, internalImageSize / 2.0);
        affineTransform.scale(scaleFactor, scaleFactor);

        ExecutorService executorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        try
        {
            logger.info("Rendering model to: " + outputPngFilePath);

            TaskCompletion taskCompletion = builtModel.render(
                    internalImage,
                    true,
                    affineTransform,
                    BlendingModes.NORMAL,
                    executorService
            );

            taskCompletion.join();

            BufferedImage outputImage = new BufferedImage(outputImageSize, outputImageSize, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics2D = outputImage.createGraphics();
            try
            {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.drawImage(internalImage, 0, 0, outputImageSize, outputImageSize, null);
            }
            finally
            {
                graphics2D.dispose();
            }

            Files.createDirectories(outputPngFilePath.getParent());
            ImageIO.write(outputImage, "PNG", outputPngFilePath.toFile());

            logger.info("Render finished. Wrote: " + outputPngFilePath);
        }
        finally
        {
            executorService.shutdownNow();
        }
    }
}