package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ModelPathResolvingWorker
{
    private final Logger logger;

    public ModelPathResolvingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public Path resolveModelJsonFilePath(Path cacheAssetsDirectoryPath, String namespaceAndIdText) throws IOException
    {
        String[] parts = namespaceAndIdText.split(":", 2);
        if (parts.length != 2)
        {
            throw new IllegalStateException("Invalid namespace:id: " + namespaceAndIdText);
        }

        String namespace = parts[0];
        String idPath = parts[1];

        if (idPath.startsWith("/"))
        {
            idPath = idPath.substring(1);
        }

        String modelFileName = idPath + ".json";

        Path namespaceModelsDirectoryPath = cacheAssetsDirectoryPath.resolve(namespace).resolve("models");
        if (!Files.exists(namespaceModelsDirectoryPath))
        {
            throw new IllegalStateException("Namespace models directory does not exist: " + namespaceModelsDirectoryPath);
        }

        List<Path> candidatePaths = new ArrayList<>();

        // Common Minecraft / ItemsAdder patterns
        candidatePaths.add(namespaceModelsDirectoryPath.resolve("item").resolve(idPath + ".json"));
        candidatePaths.add(namespaceModelsDirectoryPath.resolve("item").resolve("ia_auto").resolve(idPath + ".json"));
        candidatePaths.add(namespaceModelsDirectoryPath.resolve("block").resolve(idPath + ".json"));
        candidatePaths.add(namespaceModelsDirectoryPath.resolve(idPath + ".json"));

        for (Path candidatePath : candidatePaths)
        {
            if (Files.exists(candidatePath))
            {
                logger.info("Resolved model json (fast path): " + candidatePath);
                return candidatePath;
            }
        }

        // Slow fallback: search for a matching filename anywhere in assets/<ns>/models/**
        logger.info("Fast model resolve failed. Searching in: " + namespaceModelsDirectoryPath);

        try (Stream<Path> pathStream = Files.walk(namespaceModelsDirectoryPath))
        {
            List<Path> matches = pathStream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(modelFileName))
                    .limit(10)
                    .toList();

            if (!matches.isEmpty())
            {
                if (matches.size() > 1)
                {
                    logger.warn("Multiple model json matches for " + namespaceAndIdText + ": " + matches);
                }

                logger.info("Resolved model json (search): " + matches.get(0));
                return matches.get(0);
            }
        }

        throw new IllegalStateException("Could not resolve model json for: " + namespaceAndIdText + " inside: " + namespaceModelsDirectoryPath);
    }
}