package hut.dev.bedrockadderrenderer.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingWorker
{
    private LoggingWorker()
    {
    }

    public static Logger createLogger(Class<?> loggerOwnerClass)
    {
        return LoggerFactory.getLogger(loggerOwnerClass);
    }
}