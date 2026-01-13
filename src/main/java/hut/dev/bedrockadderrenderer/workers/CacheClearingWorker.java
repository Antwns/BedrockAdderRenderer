package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class CacheClearingWorker
{
    private final Logger logger;

    public CacheClearingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void clearPackCache(Path cacheDirectoryPath) throws IOException
    {
        if (cacheDirectoryPath == null)
        {
            throw new IllegalArgumentException("cacheDirectoryPath is null");
        }

        Path cacheAssetsDirectoryPath = cacheDirectoryPath.resolve("assets");
        Path packStateFilePath = cacheDirectoryPath.resolve("pack_state.json");

        if (Files.exists(cacheAssetsDirectoryPath))
        {
            logger.info("Deleting cache assets directory: " + cacheAssetsDirectoryPath.toAbsolutePath());
            deleteRecursively(cacheAssetsDirectoryPath);
        }

        if (Files.exists(packStateFilePath))
        {
            logger.info("Deleting pack state file: " + packStateFilePath.toAbsolutePath());
            Files.delete(packStateFilePath);
        }

        logger.info("clear-pack finished successfully.");
    }

    private void deleteRecursively(Path rootPath) throws IOException
    {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
