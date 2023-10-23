package liquibase.logging.core;

import liquibase.logging.LogMessageFilter;
import liquibase.logging.Logger;

import java.util.List;
import java.util.logging.Level;

public class CompositeLogger extends AbstractLogger {

    private final List<Logger> loggers;

    /**
     * @deprecated use {@link #CompositeLogger(List)}
     */
    public CompositeLogger(List<Logger> loggers, LogMessageFilter filter) {
        this(loggers);
    }

    public CompositeLogger(List<Logger> loggers) {
        this.loggers = loggers;
    }

    @Override
    public void close() throws Exception {
        for (Logger logger : loggers) {
            logger.close();
        }
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        if (level == Level.OFF) {
            return;
        }

        for (Logger logger : loggers) {
            logger.log(level, message, e);
        }

    }
}
