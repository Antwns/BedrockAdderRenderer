package hut.dev.bedrockadderrenderer.workers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModelJsonReadingWorker
{
    private final Logger logger;
    private final Gson gson;

    public ModelJsonReadingWorker(Logger logger)
    {
        this.logger = logger;
        this.gson = new GsonBuilder().create();
    }

    public MinecraftModelJson readModelJson(Path modelJsonFilePath) throws Exception
    {
        logger.info("Reading model json: " + modelJsonFilePath);
        String jsonText = Files.readString(modelJsonFilePath, StandardCharsets.UTF_8);
        MinecraftModelJson parsed = gson.fromJson(jsonText, MinecraftModelJson.class);

        if (parsed == null)
        {
            throw new IllegalStateException("Failed to parse model json: " + modelJsonFilePath);
        }

        return parsed;
    }
}