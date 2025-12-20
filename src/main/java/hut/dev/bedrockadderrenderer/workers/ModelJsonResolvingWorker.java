package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ModelJsonResolvingWorker
{
    private final Logger logger;

    public ModelJsonResolvingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public MinecraftModelJson readResolvedModelJson(Path modelJsonFilePath, Path cacheAssetsDirectoryPath) throws Exception
    {
        return readResolvedModelJsonInternal(modelJsonFilePath, cacheAssetsDirectoryPath, new HashSet<>());
    }

    private MinecraftModelJson readResolvedModelJsonInternal(Path modelJsonFilePath, Path cacheAssetsDirectoryPath, Set<Path> visited) throws Exception
    {
        Path normalizedPath = modelJsonFilePath.toAbsolutePath().normalize();

        if (visited.contains(normalizedPath))
        {
            throw new IllegalStateException("Parent model cycle detected at: " + normalizedPath);
        }

        visited.add(normalizedPath);

        MinecraftModelJson childModelJson = new ModelJsonReadingWorker(logger).readModelJson(modelJsonFilePath);

        if (childModelJson.parent == null || childModelJson.parent.isBlank())
        {
            return childModelJson;
        }

        Path parentModelJsonFilePath = new ParentModelPathResolvingWorker().resolveParentModelJsonFilePath(cacheAssetsDirectoryPath, childModelJson.parent);
        if (!Files.exists(parentModelJsonFilePath))
        {
            throw new IllegalStateException("Parent model not found: " + childModelJson.parent + " -> " + parentModelJsonFilePath);
        }

        MinecraftModelJson resolvedParentModelJson = readResolvedModelJsonInternal(parentModelJsonFilePath, cacheAssetsDirectoryPath, visited);

        MinecraftModelJson mergedModelJson = new MinecraftModelJson();

        // ---- elements ----
        boolean childHasElements = childModelJson.elements != null && !childModelJson.elements.isEmpty();
        mergedModelJson.elements = childHasElements ? childModelJson.elements : resolvedParentModelJson.elements;

        // ---- textures ----
        Map<String, String> mergedTextures = new HashMap<>();
        if (resolvedParentModelJson.textures != null)
        {
            mergedTextures.putAll(resolvedParentModelJson.textures);
        }
        if (childModelJson.textures != null)
        {
            mergedTextures.putAll(childModelJson.textures);
        }
        mergedModelJson.textures = mergedTextures;

        // ---- display ----
        mergedModelJson.display = (childModelJson.display != null) ? childModelJson.display : resolvedParentModelJson.display;

        // ---- gui_light ----
        mergedModelJson.gui_light = (childModelJson.gui_light != null) ? childModelJson.gui_light : resolvedParentModelJson.gui_light;

        // ---- copy metadata (optional, but fine) ----
        mergedModelJson.credit = (childModelJson.credit != null) ? childModelJson.credit : resolvedParentModelJson.credit;
        mergedModelJson.format_version = (childModelJson.format_version != null) ? childModelJson.format_version : resolvedParentModelJson.format_version;

        // resolved result has no parent
        mergedModelJson.parent = null;

        logger.info("Resolved parent chain for: " + modelJsonFilePath.getFileName() + " (parent was: " + childModelJson.parent + ")");

        return mergedModelJson;
    }
}