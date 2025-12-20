package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.util.Map;

public final class TextureResolvingWorker
{
    private final Logger logger;

    public TextureResolvingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public String resolveTextureReference(Map<String, String> texturesMap, String textureText)
    {
        if (textureText == null)
        {
            return null;
        }

        String current = textureText;

        int safetyCounter = 0;
        while (current.startsWith("#"))
        {
            safetyCounter++;
            if (safetyCounter > 50)
            {
                throw new IllegalStateException("Texture reference loop detected: " + textureText);
            }

            String key = current.substring(1);
            String resolved = texturesMap.get(key);
            if (resolved == null)
            {
                return null;
            }

            current = resolved;
        }

        return current;
    }
}