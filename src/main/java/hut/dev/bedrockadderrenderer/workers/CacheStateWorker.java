package hut.dev.bedrockadderrenderer.workers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public final class CacheStateWorker
{
    private final Logger logger;
    private final Gson gson;

    public CacheStateWorker(Logger logger)
    {
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void writePackState(Path packStateFilePath, Path itemsAdderGeneratedZipPath, Path vanillaJarPath, Path cacheDirectoryPath) throws IOException
    {
        Files.createDirectories(packStateFilePath.getParent());

        PackState packState = new PackState();
        packState.itemsAdderGeneratedZipPath = itemsAdderGeneratedZipPath.toAbsolutePath().toString();
        packState.vanillaJarPath = vanillaJarPath.toAbsolutePath().toString();
        packState.cacheDirectoryPath = cacheDirectoryPath.toAbsolutePath().toString();
        packState.createdAt = OffsetDateTime.now().toString();

        String jsonText = gson.toJson(packState);
        Files.writeString(packStateFilePath, jsonText, StandardCharsets.UTF_8);

        logger.info("Wrote pack state: " + packStateFilePath);
    }

    private static final class PackState
    {
        public String itemsAdderGeneratedZipPath;
        public String vanillaJarPath;
        public String cacheDirectoryPath;
        public String createdAt;
    }
}