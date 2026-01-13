package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minecraft Java animated textures are stored as a vertical strip of frames.
 * When rendering static icons we want the first frame only.
 *
 * If we render with the full animated PNG height, our UV crop math will use the
 * wrong vertical scale, which causes faces to appear stretched / wrong.
 */
public final class AnimatedTextureFirstFrameWorker
{
    private final Logger logger;

    public AnimatedTextureFirstFrameWorker(Logger logger)
    {
        this.logger = logger;
    }

    public BufferedImage getFirstFrameIfAnimated(Path texturePngPath, BufferedImage loadedTexture)
    {
        if (loadedTexture == null)
        {
            return null;
        }

        if (!looksAnimated(texturePngPath, loadedTexture))
        {
            return loadedTexture;
        }

        int frameWidth = loadedTexture.getWidth();
        int frameHeight = frameWidth;

        if (frameWidth <= 0 || loadedTexture.getHeight() < frameHeight)
        {
            return loadedTexture;
        }

        BufferedImage firstFrameView = loadedTexture.getSubimage(0, 0, frameWidth, frameHeight);

        // Copy into a standalone image so we don't keep the whole strip referenced.
        BufferedImage copied = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics graphics = copied.getGraphics();
        try
        {
            graphics.drawImage(firstFrameView, 0, 0, null);
        }
        finally
        {
            graphics.dispose();
        }

        logger.debug("Animated texture detected; using first frame only: " + texturePngPath.getFileName());

        return copied;
    }

    private boolean looksAnimated(Path texturePngPath, BufferedImage loadedTexture)
    {
        // Most reliable signal is existence of .png.mcmeta
        Path mcmetaPath = texturePngPath.resolveSibling(texturePngPath.getFileName().toString() + ".mcmeta");
        if (Files.exists(mcmetaPath))
        {
            return true;
        }

        // Fallback heuristic: vertical strip (height > width and divisible by width)
        int w = loadedTexture.getWidth();
        int h = loadedTexture.getHeight();

        if (w <= 0)
        {
            return false;
        }

        return h > w && (h % w == 0);
    }
}
