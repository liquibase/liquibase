package liquibase.logging.core;

import java.util.logging.Level;

/**
 * Logger which does nothing with the log records it is provided.
 */
public class NoOpLogger extends AbstractLogger {
    @Override
    public void log(Level level, String message, Throwable e) {
        // intentionally do nothing
    }
}
