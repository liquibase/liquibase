package liquibase.logging.core;

import liquibase.logging.LoggerContext;

/**
 * "Blank" context to use for {@link liquibase.logging.LoggerFactory} implementations that do not support nested contexts.
 */
public class NoOpLoggerContext implements LoggerContext {

    @Override
    public void close() {

    }

    @Override
    public void showMoreProgress() {

    }

    @Override
    public void showMoreProgress(int percentComplete) {

    }
}
