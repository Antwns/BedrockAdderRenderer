package hut.dev.bedrockadderrenderer.workers;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathResolvingWorker
{
    public Path resolveCacheDirectoryPath(String[] args, int optionalCacheIndex)
    {
        if (args.length > optionalCacheIndex)
        {
            return Paths.get(args[optionalCacheIndex]);
        }

        return Paths.get("cache");
    }
}