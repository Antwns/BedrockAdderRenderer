package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackExtractionWorker
{
    private final Logger logger;

    public PackExtractionWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void extractVanillaJarAssets(Path vanillaJarPath, Path cacheAssetsDirectoryPath) throws IOException
    {
        logger.info("Extracting vanilla jar assets from: " + vanillaJarPath);
        extractZipAssets(vanillaJarPath, cacheAssetsDirectoryPath, "assets/");
    }

    public void extractItemsAdderGeneratedZipAssets(Path itemsAdderGeneratedZipPath, Path cacheAssetsDirectoryPath) throws IOException
    {
        logger.info("Extracting ItemsAdder generated.zip assets from: " + itemsAdderGeneratedZipPath);
        extractZipAssets(itemsAdderGeneratedZipPath, cacheAssetsDirectoryPath, "assets/");
    }

    private void extractZipAssets(Path zipFilePath, Path cacheAssetsDirectoryPath, String requiredPrefix) throws IOException
    {
        Files.createDirectories(cacheAssetsDirectoryPath);

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile()))
        {
            zipFile.stream().forEach(zipEntry ->
            {
                if (zipEntry.isDirectory())
                {
                    return;
                }

                String entryName = zipEntry.getName().replace('\\', '/');
                if (!entryName.startsWith(requiredPrefix))
                {
                    return;
                }
                String relativeEntryName = entryName.substring(requiredPrefix.length()); // strip "assets/"
                Path targetFilePath = cacheAssetsDirectoryPath.resolve(relativeEntryName);
                try
                {
                    Files.createDirectories(targetFilePath.getParent());

                    try (InputStream entryInputStream = zipFile.getInputStream(zipEntry))
                    {
                        Files.copy(entryInputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                catch (IOException exception)
                {
                    throw new RuntimeException("Failed to extract: " + entryName, exception);
                }
            });
        }
    }
}