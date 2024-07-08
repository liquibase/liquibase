package liquibase.logging.core;

import liquibase.logging.Logger;

/**
 * Log service for the {@link NoOpLogger} which does nothing with the log records it is provided.
 */
public class NoOpLogService extends AbstractLogService {

    private static final NoOpLogger NO_OP_LOGGER = new NoOpLogger();

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        return NO_OP_LOGGER;
    }
}
