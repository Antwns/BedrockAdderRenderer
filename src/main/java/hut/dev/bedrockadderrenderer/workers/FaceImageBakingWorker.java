package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;

public final class FaceImageBakingWorker
{
    private final Logger logger;

    public FaceImageBakingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public BufferedImage bakeFaceImage(
            String faceName,
            MinecraftModelElementJson elementJson,
            MinecraftModelJson modelJson,
            java.nio.file.Path cacheAssetsDirectoryPath,
            TextureResolvingWorker textureResolvingWorker,
            TextureLoadingWorker textureLoadingWorker,
            Map<String, BufferedImage> loadedTextureCache
    ) throws Exception
    {
        if (elementJson.faces == null)
        {
            return createTransparentImage();
        }

        MinecraftModelFaceJson faceJson = elementJson.faces.get(faceName);

        if (faceJson == null || faceJson.texture == null)
        {
            return createTransparentImage();
        }

        String resolvedTextureResourceLocation = textureResolvingWorker.resolveTextureReference(modelJson.textures, faceJson.texture);
        if (resolvedTextureResourceLocation == null)
        {
            return createTransparentImage();
        }

        BufferedImage fullTextureImage = loadedTextureCache.get(resolvedTextureResourceLocation);
        if (fullTextureImage == null)
        {
            fullTextureImage = textureLoadingWorker.loadTextureImageFromResourceLocation(cacheAssetsDirectoryPath, resolvedTextureResourceLocation);
            loadedTextureCache.put(resolvedTextureResourceLocation, fullTextureImage);
        }

        BufferedImage cropped = cropUv(fullTextureImage, faceJson.uv);

        int rotationDegrees = 0;
        if (faceJson.rotation != null)
        {
            rotationDegrees = faceJson.rotation;
        }

        if (rotationDegrees != 0)
        {
            cropped = rotateImageByDegrees(cropped, rotationDegrees);
        }

        return cropped;
    }

    private BufferedImage cropUv(BufferedImage textureImage, double[] uv)
    {
        if (uv == null || uv.length != 4)
        {
            return textureImage;
        }

        double scaleX = textureImage.getWidth() / 16.0;
        double scaleY = textureImage.getHeight() / 16.0;

        double u1 = uv[0];
        double v1 = uv[1];
        double u2 = uv[2];
        double v2 = uv[3];

        boolean flipHorizontally = u2 < u1;
        boolean flipVertically = v2 < v1;

        double minU = Math.min(u1, u2);
        double maxU = Math.max(u1, u2);
        double minV = Math.min(v1, v2);
        double maxV = Math.max(v1, v2);

        int x1 = (int)Math.round(minU * scaleX);
        int y1 = (int)Math.round(minV * scaleY);
        int x2 = (int)Math.round(maxU * scaleX);
        int y2 = (int)Math.round(maxV * scaleY);

        x1 = clamp(x1, 0, textureImage.getWidth());
        x2 = clamp(x2, 0, textureImage.getWidth());
        y1 = clamp(y1, 0, textureImage.getHeight());
        y2 = clamp(y2, 0, textureImage.getHeight());

        int width = Math.max(1, x2 - x1);
        int height = Math.max(1, y2 - y1);

        BufferedImage cropped = textureImage.getSubimage(x1, y1, width, height);

        if (flipHorizontally || flipVertically)
        {
            cropped = flipImage(cropped, flipHorizontally, flipVertically);
        }

        return cropped;
    }

    private BufferedImage flipImage(BufferedImage input, boolean flipHorizontally, boolean flipVertically)
    {
        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        java.awt.Graphics2D graphics2D = output.createGraphics();
        try
        {
            java.awt.geom.AffineTransform affineTransform = new java.awt.geom.AffineTransform();

            affineTransform.translate(flipHorizontally ? width : 0, flipVertically ? height : 0);
            affineTransform.scale(flipHorizontally ? -1 : 1, flipVertically ? -1 : 1);

            graphics2D.drawImage(input, affineTransform, null);
        }
        finally
        {
            graphics2D.dispose();
        }

        return output;
    }

    private int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private BufferedImage rotateImageByDegrees(BufferedImage input, int degrees)
    {
        int normalized = ((degrees % 360) + 360) % 360;
        if (normalized == 0)
        {
            return input;
        }

        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output;
        if (normalized == 90 || normalized == 270)
        {
            output = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D graphics2D = output.createGraphics();
        try
        {
            AffineTransform affineTransform = new AffineTransform();

            if (normalized == 90)
            {
                affineTransform.translate(height, 0);
                affineTransform.rotate(Math.toRadians(90));
            }
            else if (normalized == 180)
            {
                affineTransform.translate(width, height);
                affineTransform.rotate(Math.toRadians(180));
            }
            else if (normalized == 270)
            {
                affineTransform.translate(0, width);
                affineTransform.rotate(Math.toRadians(270));
            }

            graphics2D.drawImage(input, affineTransform, null);
        }
        finally
        {
            graphics2D.dispose();
        }

        return output;
    }

    private BufferedImage createTransparentImage()
    {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
}