package hut.dev.bedrockadderrenderer;

import hut.dev.bedrockadderrenderer.workers.CommandRoutingWorker;
import hut.dev.bedrockadderrenderer.workers.LoggingWorker;
import org.slf4j.Logger;

public final class BedrockAdderRendererMain
{
    private BedrockAdderRendererMain()
    {
    }

    public static void main(String[] args)
    {
        Logger logger = LoggingWorker.createLogger(BedrockAdderRendererMain.class);

        try
        {
            int exitCode = new CommandRoutingWorker(logger).routeCommand(args);
            System.exit(exitCode);
        }
        catch (Exception exception)
        {
            logger.error("Fatal error", exception);
            System.exit(1);
        }
    }
}