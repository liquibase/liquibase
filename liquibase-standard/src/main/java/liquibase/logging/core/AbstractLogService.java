package liquibase.logging.core;

import liquibase.logging.LogMessageFilter;
import liquibase.logging.LogService;

/**
 * Convenience base implementation of a LoggerFactory.
 */
public abstract class AbstractLogService implements LogService {

    /**
     * Default implementation does nothing.
     */
    @Override
    public void close() {

    }

    /**
     * @deprecated always returns null
     */
    @Deprecated
    @Override
    public LogMessageFilter getFilter() {
        return null;
    }

    /**
     * @deprecated does not save the filter
     */
    @Deprecated
    @Override
    public void setFilter(LogMessageFilter filter) {
    }
}
