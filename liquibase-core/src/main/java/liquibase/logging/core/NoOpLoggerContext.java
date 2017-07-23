package liquibase.logging.core;

import liquibase.logging.LoggerContext;

public class NoOpLoggerContext implements LoggerContext {

    @Override
    public void close() {

    }

    @Override
    public void showProgress() {

    }

    @Override
    public void showProgress(int percentComplete) {

    }
}
