package liquibase.logging.core;

import liquibase.logging.LogService;
import liquibase.logging.Logger;

import java.util.logging.Level;

/**
 * Convenience base implementation of a LoggerFactory.
 */
public abstract class AbstractLogService implements LogService {

    public AbstractLogService() {
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public void close() {

    }

}
