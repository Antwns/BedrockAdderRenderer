package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TextureLoadingWorker
{
    private final Logger logger;

    public TextureLoadingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public BufferedImage loadTextureImageFromResourceLocation(Path cacheAssetsDirectoryPath, String resourceLocationText) throws Exception
    {
        // Example: "workbenches:advanced_brewing_stand/torre"
        String[] parts = resourceLocationText.split(":", 2);
        if (parts.length != 2)
        {
            throw new IllegalStateException("Invalid resource location: " + resourceLocationText);
        }

        String namespace = parts[0];
        String pathPart = parts[1];

        if (!pathPart.startsWith("textures/"))
        {
            pathPart = "textures/" + pathPart;
        }
        if (!pathPart.endsWith(".png"))
        {
            pathPart = pathPart + ".png";
        }

        Path textureFilePath = cacheAssetsDirectoryPath.resolve(namespace).resolve(pathPart);

        if (!Files.exists(textureFilePath))
        {
            throw new IllegalStateException("Texture PNG not found: " + textureFilePath + " (from " + resourceLocationText + ")");
        }

        BufferedImage loaded = ImageIO.read(textureFilePath.toFile());
        if (loaded == null)
        {
            throw new IllegalStateException("ImageIO failed to read: " + textureFilePath);
        }

        return loaded;
    }
}