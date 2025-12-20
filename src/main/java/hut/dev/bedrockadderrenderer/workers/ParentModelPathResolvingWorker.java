package hut.dev.bedrockadderrenderer.workers;

import java.nio.file.Path;

public final class ParentModelPathResolvingWorker
{
    public Path resolveParentModelJsonFilePath(Path cacheAssetsDirectoryPath, String parentText)
    {
        String namespace = "minecraft";
        String modelPath = parentText;

        if (parentText.contains(":"))
        {
            String[] split = parentText.split(":", 2);
            namespace = split[0];
            modelPath = split[1];
        }

        // Minecraft model references are relative to models/ and omit ".json"
        // e.g. "block/cube_all" -> assets/minecraft/models/block/cube_all.json
        return cacheAssetsDirectoryPath
                .resolve(namespace)
                .resolve("models")
                .resolve(modelPath + ".json");
    }
}