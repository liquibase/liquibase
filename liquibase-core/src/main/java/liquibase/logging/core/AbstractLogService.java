package liquibase.logging.core;

import liquibase.logging.LogMessageFilter;
import liquibase.logging.LogService;
import liquibase.logging.Logger;

import java.util.logging.Level;

/**
 * Convenience base implementation of a LoggerFactory.
 */
public abstract class AbstractLogService implements LogService {

    protected LogMessageFilter filter;

    public AbstractLogService() {
        this.filter = new DefaultLogMessageFilter();
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public void close() {

    }

    @Override
    public LogMessageFilter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(LogMessageFilter filter) {
        this.filter = filter;
    }
}
