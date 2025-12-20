package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;

public final class CommandRoutingWorker
{
    private final Logger logger;

    public CommandRoutingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public int routeCommand(String[] args)
    {
        if (args == null || args.length == 0)
        {
            printUsage();
            return 1;
        }

        String commandName = args[0];

        if (commandName.equalsIgnoreCase("help"))
        {
            printUsage();
            return 0;
        }
        if (commandName.equalsIgnoreCase("render-model-file"))
        {
            logger.info("  BedrockAdderRenderer render-model-file <size> <model_json> <output_png> [cacheDir]");

            if (args.length < 4)
            {
                logger.error("render-model-file requires: <size> <model_json> <output_png> [cacheDir]");
                return 1;
            }

            int outputImageSize;
            try
            {
                outputImageSize = Integer.parseInt(args[1]);
            }
            catch (Exception exception)
            {
                logger.error("Invalid size: " + args[1]);
                return 1;
            }

            java.nio.file.Path modelJsonFilePath = java.nio.file.Paths.get(args[2]);
            java.nio.file.Path outputPngFilePath = java.nio.file.Paths.get(args[3]);

            java.nio.file.Path cacheDirectoryPath = new PathResolvingWorker().resolveCacheDirectoryPath(args, 4);
            java.nio.file.Path cacheAssetsDirectoryPath = cacheDirectoryPath.resolve("assets");

            try
            {
                new ModelRenderWorker(logger).renderModelJsonFile(modelJsonFilePath, cacheAssetsDirectoryPath, outputImageSize, outputPngFilePath);
                return 0;
            }
            catch (Exception exception)
            {
                logger.error("render-model-file failed", exception);
                return 1;
            }
        }
        if (commandName.equalsIgnoreCase("set-pack"))
        {
                if (args.length < 3)
                {
                    logger.error("set-pack requires: <itemsadder_generated_zip> <vanilla_client_jar> [cacheDir]");
                    printUsage();
                    return 1;
                }

                String itemsAdderGeneratedZipPathText = args[1];
                String vanillaClientJarPathText = args[2];

                java.nio.file.Path cacheDirectoryPath = new PathResolvingWorker().resolveCacheDirectoryPath(args, 3);
                java.nio.file.Path cacheAssetsDirectoryPath = cacheDirectoryPath.resolve("assets");
                java.nio.file.Path packStateFilePath = cacheDirectoryPath.resolve("pack_state.json");

                java.nio.file.Path itemsAdderGeneratedZipPath = java.nio.file.Paths.get(itemsAdderGeneratedZipPathText);
                java.nio.file.Path vanillaClientJarPath = java.nio.file.Paths.get(vanillaClientJarPathText);

                if (!java.nio.file.Files.exists(itemsAdderGeneratedZipPath))
                {
                    logger.error("ItemsAdder generated.zip not found: " + itemsAdderGeneratedZipPath);
                    return 1;
                }

                if (!java.nio.file.Files.exists(vanillaClientJarPath))
                {
                    logger.error("Vanilla jar not found: " + vanillaClientJarPath);
                    return 1;
                }

                logger.info("Cache directory: " + cacheDirectoryPath.toAbsolutePath());

                try
                {
                    PackExtractionWorker packExtractionWorker = new PackExtractionWorker(logger);

                    // Vanilla first, then IA overwrites it.
                    packExtractionWorker.extractVanillaJarAssets(vanillaClientJarPath, cacheAssetsDirectoryPath);
                    packExtractionWorker.extractItemsAdderGeneratedZipAssets(itemsAdderGeneratedZipPath, cacheAssetsDirectoryPath);

                    new CacheStateWorker(logger).writePackState(packStateFilePath, itemsAdderGeneratedZipPath, vanillaClientJarPath, cacheDirectoryPath);

                    logger.info("set-pack finished successfully.");
                    return 0;
                }
                catch (Exception exception) {
                    logger.error("set-pack failed", exception);
                    return 1;
                }
        }
        if (commandName.equalsIgnoreCase("render-test"))
        {
            if (args.length < 4)
            {
                logger.error("render-test requires: <size> <texture_png> <output_png>");

                return 1;
            }

            int outputImageSize;
            try
            {
                outputImageSize = Integer.parseInt(args[1]);
            }
            catch (Exception exception)
            {
                logger.error("Invalid size: " + args[1]);
                return 1;
            }

            String texturePngFilePathText = args[2];
            String outputPngFilePathText = args[3];

            try
            {
                java.nio.file.Path texturePngFilePath = java.nio.file.Paths.get(texturePngFilePathText);
                java.nio.file.Path outputPngFilePath = java.nio.file.Paths.get(outputPngFilePathText);

                new RenderTestWorker(logger).renderCookieBlockCube(texturePngFilePath, outputImageSize, outputPngFilePath);
                return 0;
            }
            catch (Exception exception)
            {
                logger.error("render-test failed", exception);
                return 1;
            }
        }
        if (commandName.equalsIgnoreCase("render"))
        {
            if (args.length < 4)
            {
                logger.error("render requires: <size> <namespace:id> <output_png> [cacheDir]");
                printUsage();
                return 1;
            }

            int outputImageSize;
            try
            {
                outputImageSize = Integer.parseInt(args[1]);
            }
            catch (Exception exception)
            {
                logger.error("Invalid size: " + args[1]);
                return 1;
            }

            String namespaceAndIdText = args[2];
            java.nio.file.Path outputPngFilePath = java.nio.file.Paths.get(args[3]);

            java.nio.file.Path cacheDirectoryPath = new PathResolvingWorker().resolveCacheDirectoryPath(args, 4);
            java.nio.file.Path cacheAssetsDirectoryPath = cacheDirectoryPath.resolve("assets");

            try
            {
                new ModelRenderWorker(logger).renderModelByNamespaceId(cacheAssetsDirectoryPath, outputImageSize, namespaceAndIdText, outputPngFilePath);
                return 0;
            }
            catch (Exception exception)
            {
                logger.error("render failed", exception);
                return 1;
            }
        }
        if (commandName.equalsIgnoreCase("clear-pack"))
        {
            logger.info("clear-pack called (not implemented yet)");
            return 0;
        }

        logger.error("Unknown command: " + commandName);
        printUsage();
        return 1;
    }

    private void printUsage()
    {
        logger.info("Usage:");
        logger.info("  BedrockAdderRenderer help");
        logger.info("  BedrockAdderRenderer render-model-file <size> <model_json> <output_png> [cacheDir]");
        logger.info("  BedrockAdderRenderer render-test <size> <texture_png> <output_png>");
        logger.info("  BedrockAdderRenderer set-pack <itemsadder_generated_zip> <vanilla_client_jar> [cacheDir]");
        logger.info("  BedrockAdderRenderer render <size> <namespace:id> <output_png> [cacheDir]");
        logger.info("  BedrockAdderRenderer clear-pack [cacheDir]");
    }
}